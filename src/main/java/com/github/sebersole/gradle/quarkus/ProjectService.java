package com.github.sebersole.gradle.quarkus;

import java.util.Map;
import java.util.TreeMap;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StrictModuleIdentifierComparator;

/**
 * @author Steve Ebersole
 */
public class ProjectService {
	private final ProjectInfo mainProjectInfo;
	private final Map<ModuleIdentifier,ProjectInfo> projectById = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );


	public ProjectService(Services services, Project mainProject) {
		final StandardModuleVersionIdentifier mainProjectIdentifier = Helper.moduleVersionIdentifier( mainProject );
		this.mainProjectInfo = services.getBuildDetails().getMainProjectInfo();
		this.projectById.put( mainProjectIdentifier, mainProjectInfo );

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

	public ProjectInfo getMainProjectInfo() {
		return mainProjectInfo;
	}

	public ProjectInfo getProjectInfo(ModuleIdentifier identifier) {
		return projectById.get( identifier );
	}

	public ProjectInfo getProjectInfoByPath(String projectPath) {
		for ( ProjectInfo info : projectById.values() ) {
			if ( info.getProjectPath().equals( projectPath ) ) {
				return info;
			}
		}
		return null;
	}

	private static ProjectInfo generateInfo(Project project) {
		final StandardModuleVersionIdentifier identifier = Helper.moduleVersionIdentifier( project );
		return new ProjectInfo( identifier, project );
	}

}
