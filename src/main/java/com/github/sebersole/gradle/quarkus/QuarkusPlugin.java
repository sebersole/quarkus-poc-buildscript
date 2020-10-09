package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.extensions.AvailableExtension;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	public static final String META_INF = "META-INF/";
	public static final String EXTENSION_PROP_FILE = META_INF + "quarkus-extension.properties";
	public static final String DEPLOYMENT_ARTIFACT_KEY = "deployment-artifact";

	private final Map<ModuleVersionIdentifier, AvailableExtension<?>> availableExtensions = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

	private QuarkusSpec dsl;

	public QuarkusSpec getDsl() {
		return dsl;
	}

	public Map<ModuleVersionIdentifier, AvailableExtension<?>> getAvailableExtensions() {
		return availableExtensions;
	}

	@Override
	public void apply(Project project) {
		dsl = project.getExtensions().create( "quarkus", QuarkusSpec.class, project );

		discoverAvailableExtensions( project );

		project.getTasks().create( ShowQuarkusExtensionsTask.DSL_NAME, ShowQuarkusExtensionsTask.class );

		project.afterEvaluate( p -> resolveExtensions() );
	}

	private void resolveExtensions() {
		final PolymorphicDomainObjectContainer<ExtensionSpec> extensionSpecContainer = dsl.getExtensionSpecContainer();
		final HashSet<ModuleVersionIdentifier> unconfiguredExtensions = new HashSet<>( availableExtensions.keySet() );
		availableExtensions.forEach(
				(moduleVersionIdentifier, availableExtension) -> {
					final NamedDomainObjectSet<? extends ExtensionSpec> explicitConfigurationsByType = extensionSpecContainer.withType( availableExtension.getExtensionClass() );
					if ( explicitConfigurationsByType.isEmpty() ) {
						final ExtensionSpec extensionSpec = extensionSpecContainer.create(
								moduleVersionIdentifier.getArtifactName(),
								availableExtension.getExtensionClass()
						);
						extensionSpec.setRuntimeArtifact( moduleVersionIdentifier.groupArtifactVersion() );
						unconfiguredExtensions.remove( moduleVersionIdentifier );
					}
					else {
						explicitConfigurationsByType.forEach(
								extensionSpec -> unconfiguredExtensions.remove( extensionSpec.getRuntimeArtifact().get() )
						);
					}
				}
		);

		unconfiguredExtensions.forEach(
				moduleVersionIdentifier -> {
					final AvailableExtension<?> availableExtension = availableExtensions.get( moduleVersionIdentifier );
					final ExtensionSpec extensionSpec = extensionSpecContainer.create(
							moduleVersionIdentifier.getArtifactName(),
							availableExtension.getExtensionClass()
					);
					extensionSpec.setRuntimeArtifact( moduleVersionIdentifier.groupArtifactVersion() );
				}
		);
	}

	private void discoverAvailableExtensions(Project project) {
		// we will do some validation after using these...
		final Set<String> knownDeploymentArtifactNames = new HashSet<>();
		final Map<ModuleVersionIdentifier, ResolvedArtifact> allResolvedArtifacts = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

		// look at each artifact resolved from the build-script classpath and do a number of things.
		// ultimately we are trying to get lists of:
		//		1) all extension deployment artifacts
		//		2) all extension runtime artifacts

		final Configuration classpath = project.getBuildscript().getConfigurations().getByName( "classpath" );
		classpath.getResolvedConfiguration().getResolvedArtifacts().forEach(
				resolvedArtifact -> {
					// todo : pull over the idea of `com.github.sebersole.gradle.quarkus.dependency.ResolvedDependency`
					//		which is a `ModuleVersionIdentifierAccess` saving allocations
					// for now...
					final ModuleVersionIdentifier identifier = new StandardModuleVersionIdentifier( resolvedArtifact );
					allResolvedArtifacts.put( identifier, resolvedArtifact );

					final Properties extensionsProps = extractExtensionProperties( resolvedArtifact );
					if ( extensionsProps != null ) {
						// we have an extension (at least we found an extension prop file)
						final AvailableExtension<ExtensionSpec> availableExtension = AvailableExtension.from( resolvedArtifact, project );
						availableExtension.contribute( dsl, project );
						availableExtensions.put( identifier, availableExtension );

						final String correspondingDeploymentArtifact = extensionsProps.getProperty( DEPLOYMENT_ARTIFACT_KEY );
						if ( correspondingDeploymentArtifact != null ) {
							knownDeploymentArtifactNames.add( correspondingDeploymentArtifact );
						}
					}
				}
		);

		allResolvedArtifacts.forEach(
				(moduleVersionIdentifier, resolvedArtifact) -> {
					final StandardModuleVersionIdentifier identifier = new StandardModuleVersionIdentifier( resolvedArtifact );
					if ( knownDeploymentArtifactNames.contains( identifier.getArtifactName() ) ) {
						knownDeploymentArtifactNames.remove( identifier.getArtifactName() );
					}
				}
		);

		if ( ! knownDeploymentArtifactNames.isEmpty() ) {
			// there were "known" deployment artifacts (known from `META-INF/quarkus-extension.properties`)
			// that were not part of the resolved dependency tree.
			// todo : list them?
			project.getLogger().info( "Quarkus plugin detected known plugin that was not part of the deployment dependency tree" );
		}
	}


	private static Properties extractExtensionProperties(ResolvedArtifact resolvedArtifact) {
		final File artifactFile = resolvedArtifact.getFile();

		if ( artifactFile.isDirectory() ) {
			// try to find it relative to the directory
			return extractExtensionProperties( artifactFile );
		}

		// try as a JAR
		try {
			return extractExtensionProperties( new JarFile( artifactFile ) );
		}
		catch ( IOException e ) {
			// not a jar
		}

		return null;
	}

	private static Properties extractExtensionProperties(File directory) {
		if ( ! directory.exists() ) {
			return null;
		}

		final File file = new File( directory, EXTENSION_PROP_FILE );
		if ( ! file.exists() ) {
			return null;
		}

		assert file.isFile();

		try ( final InputStream stream = new FileInputStream( file ) ) {
			return extractExtensionProperties( stream );
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException( "should never ever ever happen, silly Java" );
		}
		catch (IOException e) {
			throw new GradleException( "Error accessing extension properties", e );
		}
	}

	private static Properties extractExtensionProperties(InputStream stream) throws IOException {
		final Properties properties = new Properties();
		properties.load( stream );
		return properties;
	}

	private static Properties extractExtensionProperties(JarFile jarFile) {
		final ZipEntry entry = jarFile.getEntry( EXTENSION_PROP_FILE );
		if ( entry == null ) {
			return null;
		}

		final Properties properties = new Properties();

		try ( final InputStream propsStream = jarFile.getInputStream( entry ) ) {
			properties.load( propsStream );
			return properties;
		}
		catch (IOException e) {
			throw new GradleException( "Error accessing the Quarkus extension properties file", e );
		}
	}
}
