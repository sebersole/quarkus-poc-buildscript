package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.ResolvedModuleVersion;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;

/**
 * General helper
 */
public class Helper {
	public static final String QUARKUS = "quarkus";
	public static final String QUARKUS_BUILD_STEPS = "quarkus build steps";

	public static final String REPORT_BANNER_LINE = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";


	public static final String META_INF = "META-INF/";

	public static final String EXTENSION_PROP_FILE = META_INF + "quarkus-extension.properties";
	public static final String DEPLOYMENT_ARTIFACT_KEY = "deployment-artifact";

	public static final String JANDEX_FILE_NAME = "jandex.idx";
	public static final String JANDEX_INDEX_FILE_PATH = META_INF + JANDEX_FILE_NAME;

	private Helper() {
		// disallow direct instantiation
	}

	public static ModuleVersionIdentifier moduleVersionIdentifier(Dependency dependency) {
		return new StandardModuleVersionIdentifier(
				dependency.getGroup(),
				dependency.getName(),
				dependency.getVersion()
		);
	}

	public static ModuleVersionIdentifier moduleVersionIdentifier(ResolvedArtifact artifact) {
		return moduleVersionIdentifier( artifact.getModuleVersion() );
	}

	public static ModuleVersionIdentifier moduleVersionIdentifier(ResolvedModuleVersion moduleVersion) {
		return new StandardModuleVersionIdentifier(
				moduleVersion.getId().getGroup(),
				moduleVersion.getId().getName(),
				moduleVersion.getId().getVersion()
		);
	}

	public static ModuleVersionIdentifier moduleVersionIdentifier(String notation) {
		if ( notation == null ) {
			return null;
		}

		final String[] splits = notation.split( ":" );
		if ( splits.length < 2 ) {
			throw new GradleException( "Too many `:` in dependency notation : " + notation );
		}
		if ( splits.length > 3 ) {
			Logging.LOGGER.debug( "Dependency notation contained more `:` separators than expected : {}", notation );
		}

		final String group = splits[0];
		final String artifact = splits[1];
		final String version;
		if ( splits.length >= 3 ) {
			version = splits[2];
		}
		else {
			version = null;
		}

		return new StandardModuleVersionIdentifier( group, artifact, version );
	}

	public static StandardModuleVersionIdentifier moduleVersionIdentifier(Project project) {
		return new StandardModuleVersionIdentifier(
				project.getGroup().toString(),
				project.getName(),
				project.getVersion().toString()
		);
	}
	public static StandardModuleVersionIdentifier moduleVersionIdentifier(ResolvedDependency gradleResolvedDependency) {
		return moduleVersionIdentifier( gradleResolvedDependency.getModule().getId() );
	}

	private static StandardModuleVersionIdentifier moduleVersionIdentifier(org.gradle.api.artifacts.ModuleVersionIdentifier gradleId) {
		return new StandardModuleVersionIdentifier(
				gradleId.getGroup(),
				gradleId.getName(),
				gradleId.getVersion()
		);
	}

	public static String groupArtifactVersion(String group, String artifact, String version) {
		assert group != null;
		assert artifact != null;
		assert version != null;

		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				group,
				artifact,
				version
		);
	}

	public static String groupArtifactVersion(Dependency dependency) {
		return groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
	}

	public static String groupArtifactVersion(ResolvedDependency resolvedDependency) {
		return groupArtifactVersion( resolvedDependency.getModuleGroup(), resolvedDependency.getModuleName(), resolvedDependency.getModuleVersion() );
	}

	public static void ensureFileExists(File file) {
		try {
			//noinspection ResultOfMethodCallIgnored
			file.getParentFile().mkdirs();
			final boolean created = file.createNewFile();
			if ( created ) {
				return;
			}
		}
		catch (IOException e) {
			Logging.LOGGER.debug( "Unable to create file {} : {}", file.getAbsolutePath(), e.getMessage() );
			return;
		}

		Logging.LOGGER.debug( "Unable to ensure File existence {}", file.getAbsolutePath() );
	}

	@FunctionalInterface
	public interface Action {
		void execute();
	}

	public static <T> T extractOnlyOne(
			Collection<T> collection,
			Supplier<T> defaultValueSupplier,
			Action tooManyAction) {
		final Iterator<T> iterator = collection.iterator();

		if ( ! iterator.hasNext() ) {
			return defaultValueSupplier.get();
		}

		final T next = iterator.next();

		if ( iterator.hasNext() ) {
			tooManyAction.execute();
			return null;
		}

		return next;
	}

	public static <T> T extractMatchingOne(Collection<T> collection, Predicate<T> matcher) {
		for ( T t : collection ) {
			if ( matcher.test( t ) ) {
				return t;
			}
		}

		return null;
	}
}
