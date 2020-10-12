package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.artifacts.ArtifactService;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;

/**
 * @author Steve Ebersole
 */
public class Services {
	// todo : BuildDetails, etc

	private final Project gradleProject;

	private final BuildDetails buildDetails;
	private final ProjectService projectService;
	private final ArtifactService artifactService;
	private final ExtensionService extensionService;

	public Services(QuarkusSpec dsl, Project gradleProject) {
		this.gradleProject = gradleProject;

		buildDetails = new BuildDetails( this, gradleProject );
		projectService = new ProjectService( this, gradleProject );
		artifactService = new ArtifactService( this, gradleProject );
		extensionService = new ExtensionService( this, dsl, gradleProject );

		artifactService.afterServicesInit();
		extensionService.afterServicesInit();

		gradleProject.getLogger().debug( "`Project#afterEvaluate` callback" );
	}

	public BuildDetails getBuildDetails() {
		return buildDetails;
	}

	public ProjectService getProjectService() {
		return projectService;
	}

	public ArtifactService getArtifactService() {
		return artifactService;
	}

	public ExtensionService getExtensionService() {
		return extensionService;
	}

	public void prepareForConfiguration() {
		gradleProject.getLogger().debug( "Preparing Services for configuration" );

		artifactService.prepareForUse();
		extensionService.prepareForUse();
	}

	public void prepareForProcessing() {
		gradleProject.getLogger().lifecycle( "Preparing Services for processing" );

		extensionService.prepareForProcessing();
	}
}
