package com.github.sebersole.gradle.quarkus.service;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.BuildDetails;
import com.github.sebersole.gradle.quarkus.ProjectService;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.artifacts.ArtifactService;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.jandex.IndexingService;

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
	private final IndexingService indexingService;

	private Map<Class<? extends Service>, Service<?>> additionalServices;

	public Services(QuarkusSpec dsl, Project gradleProject) {
		this.gradleProject = gradleProject;

		buildDetails = new BuildDetails( this, gradleProject );
		projectService = new ProjectService( this, gradleProject );
		artifactService = new ArtifactService( this, gradleProject );
		extensionService = new ExtensionService( this, dsl, gradleProject );
		indexingService = new IndexingService( this, gradleProject );

		projectService.afterServicesInit();
		artifactService.afterServicesInit();
		extensionService.afterServicesInit();
		indexingService.afterServicesInit();

		gradleProject.afterEvaluate( p -> afterProjectEvaluation() );
		gradleProject.getGradle().getTaskGraph().whenReady( graph -> afterTaskGraphReady() );
	}

	public void afterProjectEvaluation() {
		gradleProject.getLogger().trace( "Preparing Services for configuration" );

		buildDetails.afterProjectEvaluation();
		projectService.afterProjectEvaluation();
		artifactService.afterProjectEvaluation();
		extensionService.afterProjectEvaluation();
		indexingService.afterProjectEvaluation();
	}

	public void afterTaskGraphReady() {
		gradleProject.getLogger().trace( "Preparing Services for processing" );

		buildDetails.afterTaskGraphReady();
		projectService.afterTaskGraphReady();
		artifactService.afterTaskGraphReady();
		extensionService.afterTaskGraphReady();
		indexingService.afterTaskGraphReady();
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

	public IndexingService getIndexingService() {
		return indexingService;
	}

	public <T> void registerService(Service<T> service) {
		if ( additionalServices == null ) {
			additionalServices = new HashMap<>();
		}
		additionalServices.put( (Class) service.getRole(), service );
	}

	@SuppressWarnings( "unchecked" )
	public <T, S extends Service<T>> S findService(Class<S> role) {
		if ( role.isAssignableFrom( BuildDetails.class ) ) {
			return (S) projectService;
		}

		if ( role.isAssignableFrom( ProjectService.class ) ) {
			return (S) projectService;
		}

		if ( role.isAssignableFrom( IndexingService.class ) ) {
			return (S) indexingService;
		}

		if ( role.isAssignableFrom( ArtifactService.class ) ) {
			return (S) artifactService;
		}

		if ( role.isAssignableFrom( ExtensionService.class ) ) {
			return (S) extensionService;
		}

		if ( additionalServices != null ) {
			return (S) additionalServices.get( role );
		}

		return null;
	}

	public <T, S extends Service<T>> S getService(Class<S> role) {
		final S service = findService( role );
		if ( service == null ) {
			throw new GradleException( "Unknown service role : " + role.getName() );
		}
		return service;
	}
}
