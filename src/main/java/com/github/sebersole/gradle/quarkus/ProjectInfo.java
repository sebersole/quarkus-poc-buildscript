package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifierAccess;

/**
 * @author Steve Ebersole
 */
public class ProjectInfo implements ModuleVersionIdentifierAccess {
	private final ModuleVersionIdentifier identifier;

	private final Project project;
	private final SourceSet mainSourceSet;

	public ProjectInfo(ModuleVersionIdentifier identifier, Project project) {
		this.identifier = identifier;
		this.project = project;

		final JavaPluginConvention javaPluginConvention = project.getConvention().findPlugin( JavaPluginConvention.class );
		if ( javaPluginConvention == null ) {
			project.getLogger().warn( "Quarkus plugin applied to non-JVM project" );
			mainSourceSet = null;
		}
		else {
			mainSourceSet = javaPluginConvention.getSourceSets().getByName( SourceSet.MAIN_SOURCE_SET_NAME );
		}
	}

	@Override
	public ModuleVersionIdentifier getModuleVersionIdentifier() {
		return identifier;
	}

	public Project getProject() {
		return project;
	}

	public String getProjectPath() {
		return project.getPath();
	}

	public SourceSet getMainSourceSet() {
		return mainSourceSet;
	}

	@Override
	public String toString() {
		return String.format(
				"ProjectInfo(id: `%s`, path: `%s`, java?: %s",
				identifier.groupArtifactVersion(),
				project.getPath(),
				mainSourceSet != null
		);
	}
}
