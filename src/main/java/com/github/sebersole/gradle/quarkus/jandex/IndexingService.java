package com.github.sebersole.gradle.quarkus.jandex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.ProjectInfo;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.artifacts.StrictModuleIdentifierComparator;


/**
 * A service for handling Jandex indexes
 */
public class IndexingService implements Service<IndexingService> {
	private final Services services;
	private final Project gradleProject;
	private final Provider<Directory> jandexDirectoryAccess;

	private final Map<ModuleVersionIdentifier,IndexManager> indexManagers = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );
	private final Map<RegularFile, IndexManager> indexManagerByIndexFile = new HashMap<>();

	private final MutableCompositeIndex compositeIndex = new MutableCompositeIndex();

	public IndexingService(Services services, Project gradleProject) {
		this.gradleProject = gradleProject;
		assert services != null : "Services is null";
		this.services = services;

		this.jandexDirectoryAccess = services.getBuildDetails().getWorkingDirectoryProperty().dir( JandexHelper.JANDEX );
	}

	public Provider<Directory> getJandexDirectoryAccess() {
		return jandexDirectoryAccess;
	}

	public void afterServicesInit() {
		gradleProject.getLogger().trace( "`IndexingService#afterServicesInit`" );
	}

	public void afterProjectEvaluation() {
		gradleProject.getLogger().trace( "`IndexingService#prepareForUse`" );

		final boolean hasSources = services.getBuildDetails().getMainProjectMainSourceSet() != null;
		if ( hasSources ) {
			gradleProject.getLogger().trace( "Registering IndexManager for project's classes" );

			final Directory jandexDirectory = jandexDirectoryAccess.get();

			final ProjectInfo mainProjectInfo = services.getProjectService().getMainProjectInfo();

			final ModuleVersionIdentifier identifier = mainProjectInfo.getModuleVersionIdentifier();
			final ResolvedDependency resolvedDependency = services.getArtifactService().resolveDependency( identifier );

			final String indexFileName = JandexHelper.indexFileName( resolvedDependency );
			final RegularFile dependencyIndexFile = jandexDirectory.file( indexFileName );

			registerIndexManager( identifier, resolvedDependency, dependencyIndexFile );
		}
	}

	public void afterTaskGraphReady() {
		gradleProject.getLogger().trace( "`IndexingService#prepareForProcessing`" );

		final Directory jandexDirectory = jandexDirectoryAccess.get();

		final Configuration runtimeDependencies = services.getBuildDetails().getRuntimeDependencies();
		runtimeDependencies.getResolvedConfiguration().getResolvedArtifacts().forEach(
				resolvedArtifact -> {
					final ModuleVersionIdentifier identifier = Helper.moduleVersionIdentifier( resolvedArtifact );
					final ResolvedDependency resolvedDependency = services.getArtifactService().resolveDependency( identifier );

					final String indexFileName = JandexHelper.indexFileName( resolvedDependency );
					final RegularFile dependencyIndexFile = jandexDirectory.file( indexFileName );

					registerIndexManager( identifier, resolvedDependency, dependencyIndexFile );
				}
		);
	}

	private void registerIndexManager(ModuleVersionIdentifier identifier, ResolvedDependency resolvedDependency, RegularFile dependencyIndexFile) {
		final IndexManager indexManager = new IndexManager(
				identifier,
				dependencyIndexFile,
				resolvedDependency.getIndexCreator(),
				services
		);

		indexManagers.put( identifier, indexManager );
		indexManagerByIndexFile.put( dependencyIndexFile, indexManager );
	}

	public Directory getJandexDirectory() {
		return jandexDirectoryAccess.getOrNull();
	}

	public IndexManager resolveIndexManager(ModuleVersionIdentifier identifier, Supplier<IndexManager> creator) {
		final IndexManager existing = indexManagers.get( identifier );
		if ( existing != null ) {
			return existing;
		}

		final IndexManager created = creator.get();
		assert Objects.equals( identifier, created.getModuleVersionIdentifier() );

		indexManagers.put( identifier, created );
		return created;
	}

	public MutableCompositeIndex getCompositeIndex() {
		return compositeIndex;
	}

	public void forEachIndexManager(Consumer<IndexManager> indexManagerConsumer) {
		indexManagers.forEach( (identifier, indexManager) -> indexManagerConsumer.accept( indexManager ) );
	}

	public IndexManager getIndexManager(ModuleVersionIdentifier identifier) {
		return indexManagers.get( identifier );
	}

	@Override
	public Class<IndexingService> getRole() {
		return IndexingService.class;
	}
}
