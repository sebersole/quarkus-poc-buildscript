package com.github.sebersole.gradle.quarkus.artifacts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.jandex.ExternalDependencyIndexCreator;
import com.github.sebersole.gradle.quarkus.jandex.IndexCreator;

/**
 * ResolvedDependency descriptor for resolved external dependencies (Maven repo, etc)
 */
public class ExternalDependency implements ResolvedDependency {
	private final ModuleVersionIdentifier identifier;

	private final Properties extensionProperties;
	private final IndexCreator indexCreator;

	public ExternalDependency(ModuleVersionIdentifier identifier, ResolvedArtifact artifact) {
		assert identifier.getGroupName() != null;
		assert identifier.getArtifactName() != null;
		assert identifier.getVersion() != null;

		this.identifier = identifier;

		final JarFile jarFileReference = createJarFileReference( artifact );
		this.extensionProperties = extractExtensionProperties( jarFileReference );
		this.indexCreator = new ExternalDependencyIndexCreator( jarFileReference );
	}

	public ModuleVersionIdentifier getIdentifier() {
		return identifier;
	}

	public Properties getExtensionProperties() {
		return extensionProperties;
	}

	@Override
	public IndexCreator getIndexCreator() {
		return indexCreator;
	}


	private static Properties extractExtensionProperties(JarFile jarFile) {
		final ZipEntry entry = jarFile.getEntry( Helper.EXTENSION_PROP_FILE );
		if ( entry == null ) {
			return null;
		}

		try {
			try ( final InputStream stream = jarFile.getInputStream( entry ) ) {
				final Properties properties = new Properties();
				properties.load( stream );
				return properties;
			}
		}
		catch (IOException e) {
			throw new IllegalStateException( "Unable to access `" + Helper.EXTENSION_PROP_FILE + "` stream" );
		}
	}

	private static JarFile createJarFileReference(ResolvedArtifact artifact) {
		// the artifact ought to be a jar file
		final File jarPath = artifact.getFile();
		assert jarPath.exists();
		assert jarPath.isFile();

		try {
			return new JarFile( jarPath );
		}
		catch (IOException e) {
			throw new IllegalStateException( "Problem accessing artifact-file as JarFie", e );
		}
	}
}
