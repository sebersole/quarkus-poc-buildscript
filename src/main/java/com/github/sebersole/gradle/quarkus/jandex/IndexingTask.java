package com.github.sebersole.gradle.quarkus.jandex;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskState;

import com.github.sebersole.gradle.quarkus.QuarkusPlugin;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Task for managing Jandex indexes for Quarkus
 */
public class IndexingTask extends DefaultTask {
	public static final String DSL_NAME = "quarkusJandex";

	private final SetProperty<String> externalDependencyCoordinates;
	private final DirectoryProperty jandexDirectory;
	private final boolean mainProjectHasJavaSources;

	@Inject
	public IndexingTask(QuarkusSpec dsl, Services services, SourceSet mainSourceSet) {
		final ObjectFactory objectFactory = getProject().getObjects();

		mainProjectHasJavaSources = mainSourceSet != null;

		externalDependencyCoordinates = objectFactory.setProperty( String.class );
		externalDependencyCoordinates.set(
				getProject().provider(
						() -> {
							final Set<String> dependencies = new HashSet<>();
							services.getIndexingService().forEachIndexManager(
									indexManager -> dependencies.add( indexManager.getDependencyIdentifier().groupArtifactVersion() )
							);

							return dependencies;
						}
				)
		);

		jandexDirectory = objectFactory.directoryProperty();
		jandexDirectory.set( services.getIndexingService().getJandexDirectoryAccess() );
	}

	public static IndexingTask apply(QuarkusSpec dsl, Services services, Project project) {
		// we also register a task execution listener force load all indexes when
		// this task was executed and but did no work
		project.getGradle().getTaskGraph().addTaskExecutionListener(
				new TaskExecutionAdapter() {
					@Override
					public void afterExecute(Task task, TaskState state) {
						if ( task.getName().equals( DSL_NAME ) ) {
							if ( ! state.getDidWork() ) {
								// the Jandex task was part of the task graph but did
								// no work - that should indicate it is "up-to-date"
								assert state.getUpToDate();
								// and should imply that all of the
								project.getLogger().lifecycle( "Re-loading Jandex indexes" );
								services.getIndexingService().forEachIndexManager( IndexManager::readIndex );
							}
						}
						super.afterExecute( task, state );
					}
				}
		);

		final SourceSet mainSourceSet = services.getBuildDetails().getMainProjectMainSourceSet();
		final IndexingTask indexingTask = project.getTasks().create( DSL_NAME, IndexingTask.class, dsl, services, mainSourceSet );

		if ( mainSourceSet != null ) {
			indexingTask.dependsOn( mainSourceSet.getCompileJavaTaskName() );
		}

		return indexingTask;
	}

	@Input
	SetProperty<String> getExternalDependencyCoordinates() {
		return externalDependencyCoordinates;
	}

	@OutputDirectory
	public DirectoryProperty getOutputDirectory() {
		return jandexDirectory;
	}

	@TaskAction
	void execute() {
		getLogger().lifecycle( "Starting {} task", DSL_NAME );
		getLogger().trace( "Starting {} task", DSL_NAME );

		final Directory directory = getOutputDirectory().get();
		final HashSet<RegularFile> existingIndexFiles = new HashSet<>();

		getOutputDirectory().getAsFileTree().forEach(
				file -> {
					final RegularFile indexFileReference = directory.file( file.getName() );
					existingIndexFiles.add( indexFileReference );
				}
		);


		final Project project = getProject();
		final QuarkusPlugin quarkusPlugin = project.getPlugins().getPlugin( QuarkusPlugin.class );
		final Services services = quarkusPlugin.getServices();

		services.getIndexingService().forEachIndexManager(
				indexManager -> {
					indexManager.generateIndex();
					existingIndexFiles.remove( indexManager.getIndexFile() );
				}
		);

		existingIndexFiles.forEach( IndexingTask::removeNoLongerIndexedFile );
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	private static void removeNoLongerIndexedFile(RegularFile noLongerNeededIndexFile) {
		noLongerNeededIndexFile.getAsFile().delete();
	}
}
