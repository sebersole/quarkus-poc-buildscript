package com.github.sebersole.gradle.quarkus.artifacts;

import java.util.Properties;

import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.ProjectInfo;
import com.github.sebersole.gradle.quarkus.jandex.IndexCreator;
import com.github.sebersole.gradle.quarkus.jandex.ProjectIndexCreator;

/**
 * @author Steve Ebersole
 */
public class ProjectDependency implements ResolvedDependency {
	private final ModuleVersionIdentifier identifier;

	private final IndexCreator indexCreator;

	public ProjectDependency(ModuleVersionIdentifier identifier, ProjectInfo referencedProjectInfo) {
		this.identifier = identifier;

		indexCreator = new ProjectIndexCreator( referencedProjectInfo.getMainSourceSet() );
	}

	@Override
	public ModuleVersionIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public Properties getExtensionProperties() {
		// not possible for a project dependency to be an extension
		return null;
	}

	@Override
	public IndexCreator getIndexCreator() {
		return indexCreator;
	}

}
