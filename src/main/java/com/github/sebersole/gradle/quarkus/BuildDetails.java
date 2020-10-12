package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.moduleVersionIdentifier;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_DEPLOYMENT_DEPS;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_PLATFORMS;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_RUNTIME_DEPS;

/**
 * @author Steve Ebersole
 */
public class BuildDetails {
	private final Services services;
	private final Project mainGradleProject;

	private final Property<String> quarkusVersionProperty;
	private final DirectoryProperty workingDirectoryProperty;

	private final ProjectInfo mainProjectInfo;

	private final Properties applicationProperties;

	private final Configuration platforms;
	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;

	public BuildDetails(Services services, Project gradleProject) {
		this.services = services;
		this.mainGradleProject = gradleProject;

		final StandardModuleVersionIdentifier mainProjectIdentifier = moduleVersionIdentifier( gradleProject );
		mainProjectInfo = new ProjectInfo( mainProjectIdentifier, gradleProject );

		quarkusVersionProperty = gradleProject.getObjects().property( String.class );
		quarkusVersionProperty.convention( "1.7.1.Final" );

		workingDirectoryProperty = gradleProject.getObjects().directoryProperty();
		workingDirectoryProperty.convention( gradleProject.getLayout().getBuildDirectory().dir( QUARKUS ) );

		applicationProperties = loadApplicationProperties( mainProjectInfo, services );

		this.platforms = gradleProject.getConfigurations().maybeCreate( QUARKUS_PLATFORMS );
		platforms.setDescription( "Configuration for Quarkus platforms" );

		runtimeDependencies = gradleProject.getConfigurations().maybeCreate( QUARKUS_RUNTIME_DEPS );
		runtimeDependencies.setDescription( "Configuration for Quarkus runtime dependencies" );
		runtimeDependencies.extendsFrom( platforms );

		deploymentDependencies = gradleProject.getConfigurations().maybeCreate( QUARKUS_DEPLOYMENT_DEPS );
		deploymentDependencies.setDescription( "Configuration for Quarkus deployment dependencies" );
		deploymentDependencies.extendsFrom( platforms );
	}

	public Property<String> getQuarkusVersionProperty() {
		return quarkusVersionProperty;
	}

	public DirectoryProperty getWorkingDirectoryProperty() {
		return workingDirectoryProperty;
	}

	public ProjectInfo getMainProjectInfo() {
		return mainProjectInfo;
	}

	public Configuration getPlatforms() {
		return platforms;
	}

	public Configuration getRuntimeDependencies() {
		return runtimeDependencies;
	}

	public Configuration getDeploymentDependencies() {
		return deploymentDependencies;
	}

	public String getApplicationProperty(String name) {
		return getApplicationProperty( name, s -> null );
	}

	public String getApplicationProperty(String name, Function<String,String> defaultValueProducer) {
		final String property = applicationProperties.getProperty( name );
		if ( property != null ) {
			return property;
		}

		return defaultValueProducer.apply( name );
	}

	public <P> P getApplicationProperty(String name, Function<String,P> converter, Function<String,P> defaultValueProducer) {
		final String property = applicationProperties.getProperty( name );
		if ( property != null ) {
			return converter.apply( property );
		}

		return defaultValueProducer.apply( name );
	}

	private static Properties loadApplicationProperties(ProjectInfo mainProjectInfo, Services services) {
		final Properties applicationProperties = new Properties();

		// cheat a little and look at the source files
		//    - this avoids an undesirable chicken-egg problem

		final Set<File> resourceSrcDirs = mainProjectInfo.getMainSourceSet().getResources().getSrcDirs();
		for ( File resourceSrcDir : resourceSrcDirs ) {
			final File propFile = new File( new File( resourceSrcDir, "META-INF" ), "application.properties" );
			if ( propFile.exists() ) {
				try ( final FileInputStream stream = new FileInputStream( propFile ) ) {
					applicationProperties.load( stream );
					// use just the first...
					break;
				}
				catch (Exception e) {
					throw new IllegalStateException( "Unable to access `application.properties`" );
				}
			}
		}

		return applicationProperties;
	}
}
