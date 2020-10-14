package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_DEPLOYMENT_DEPS;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_PLATFORMS;
import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_RUNTIME_DEPS;

/**
 * Basic details about the build
 */
public class BuildDetails implements Service<BuildDetails> {
	private final Services services;
	private final Project mainGradleProject;
	private final SourceSet mainProjectSourceSet;

	private final Property<String> quarkusVersionProperty;
	private final DirectoryProperty workingDirectoryProperty;

	private final Properties applicationProperties;

	private final Configuration platforms;
	private final Configuration runtimeDependencies;
	private final Configuration deploymentDependencies;

	public BuildDetails(Services services, Project gradleProject) {
		this.services = services;
		this.mainGradleProject = gradleProject;

		final JavaPluginConvention javaPluginConvention = gradleProject.getConvention().findPlugin( JavaPluginConvention.class );
		if ( javaPluginConvention == null ) {
			mainProjectSourceSet = null;
		}
		else {
			mainProjectSourceSet = javaPluginConvention.getSourceSets().findByName( SourceSet.MAIN_SOURCE_SET_NAME );
		}

		quarkusVersionProperty = gradleProject.getObjects().property( String.class );
		quarkusVersionProperty.convention( "1.7.1.Final" );

		workingDirectoryProperty = gradleProject.getObjects().directoryProperty();
		workingDirectoryProperty.convention( gradleProject.getLayout().getBuildDirectory().dir( QUARKUS ) );

		applicationProperties = loadApplicationProperties( mainProjectSourceSet, services );

		platforms = gradleProject.getConfigurations().maybeCreate( QUARKUS_PLATFORMS );
		platforms.setDescription( "Configuration for Quarkus platforms" );

		runtimeDependencies = gradleProject.getConfigurations().maybeCreate( QUARKUS_RUNTIME_DEPS );
		runtimeDependencies.setDescription( "Configuration for Quarkus runtime dependencies" );
		runtimeDependencies.extendsFrom( platforms );

		deploymentDependencies = gradleProject.getConfigurations().maybeCreate( QUARKUS_DEPLOYMENT_DEPS );
		deploymentDependencies.setDescription( "Configuration for Quarkus deployment dependencies" );
		deploymentDependencies.extendsFrom( platforms );
	}

	public void afterProjectEvaluation() {
	}

	public void afterTaskGraphReady() {
	}

	public SourceSet getMainProjectMainSourceSet() {
		return mainProjectSourceSet;
	}

	public Property<String> getQuarkusVersionProperty() {
		return quarkusVersionProperty;
	}

	public DirectoryProperty getWorkingDirectoryProperty() {
		return workingDirectoryProperty;
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

	public void visitMatchingApplicationProperties(String prefix, BiConsumer<String,String> consumer) {
		applicationProperties.forEach(
				(key, value) -> {
					if ( key instanceof String && value instanceof String ) {
						final String propertyName = (String) key;
						if ( propertyName.startsWith( prefix ) ) {
							consumer.accept( propertyName, (String) value );
						}
					}
				}
		);
	}

	public Provider<String> getQuarkusVersion() {
		return quarkusVersionProperty;
	}

	public Project getMainGradleProject() {
		return mainGradleProject;
	}

	@Override
	public Class<BuildDetails> getRole() {
		return BuildDetails.class;
	}

	private static Properties loadApplicationProperties(SourceSet mainSourceSet, Services services) {
		final Properties applicationProperties = new Properties();

		// cheat a little and look at the source files
		//    - this avoids an undesirable chicken-egg problem

		if ( mainSourceSet != null ) {
			final SourceDirectorySet resourcesDirectorySet = mainSourceSet.getResources();
			final Set<File> resourceSrcDirs = resourcesDirectorySet.getSrcDirs();
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
		}

		return applicationProperties;
	}
}
