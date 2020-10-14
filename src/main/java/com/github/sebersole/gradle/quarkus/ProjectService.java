package com.github.sebersole.gradle.quarkus;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StrictModuleIdentifierComparator;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * @author Steve Ebersole
 */
public class ProjectService implements Service<ProjectService> {
	private final Project mainProject;

	private ProjectInfo mainProjectInfo;
	private Map<ModuleIdentifier,ProjectInfo> projectById;


	public ProjectService(Services services, Project mainProject) {
		this.mainProject = mainProject;
	}

	public void afterServicesInit() {
		Logging.LOGGER.trace( "ProjectService#afterServicesInit" );
	}

	public void afterProjectEvaluation() {
		Logging.LOGGER.trace( "ProjectService#afterProjectEvaluation" );

		projectById = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

		final StandardModuleVersionIdentifier mainProjectIdentifier = Helper.moduleVersionIdentifier( mainProject );
		mainProjectInfo = new ProjectInfo( mainProjectIdentifier, mainProject );
		projectById.put( mainProjectIdentifier, mainProjectInfo );

		mainProject.getRootProject().subprojects(
				project -> {
					final StandardModuleVersionIdentifier identifier = Helper.moduleVersionIdentifier( project );
					projectById.computeIfAbsent(
							identifier,
							i -> new ProjectInfo( identifier, project )
					);
				}
		);
	}

	public void afterTaskGraphReady() {
		Logging.LOGGER.trace( "ProjectService#afterTaskGraphReady" );

	}

	@Override
	public Class<ProjectService> getRole() {
		return ProjectService.class;
	}

	public Project getMainProject() {
		return mainProject;
	}

	private void notYetReadyForUse() {
		throw new IllegalStateException( "ProjectService not yet ready for use" );
	}

	public ProjectInfo getMainProjectInfo() {
		if ( mainProjectInfo == null ) {
			notYetReadyForUse();
		}

		return mainProjectInfo;
	}

	public ProjectInfo getProjectInfo(ModuleIdentifier identifier) {
		if ( mainProjectInfo == null ) {
			notYetReadyForUse();
		}

		return projectById.get( identifier );
	}

	public ProjectInfo getProjectInfoByPath(String projectPath) {
		if ( mainProjectInfo == null ) {
			notYetReadyForUse();
		}

		for ( ProjectInfo info : projectById.values() ) {
			if ( info.getProjectPath().equals( projectPath ) ) {
				return info;
			}
		}
		return null;
	}

	public void forEachProject(Consumer<ProjectInfo> consumer) {
		if ( mainProjectInfo == null ) {
			notYetReadyForUse();
		}

		projectById.forEach( (moduleIdentifier, projectInfo) -> consumer.accept( projectInfo ) );
	}
}
