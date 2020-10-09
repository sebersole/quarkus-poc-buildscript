package com.github.sebersole.gradle.quarkus;

import java.util.Locale;

/**
 * Unique identifier for a module dependency version
 */
public interface ModuleVersionIdentifier extends ModuleIdentifier {
	/**
	 * The dependency's version
	 */
	String getVersion();

	default String groupArtifactVersion() {
		return String.format(
				Locale.ROOT,
				"%s:%s:%s",
				getGroupName(),
				getArtifactName(),
				getVersion()
		);
	}
}
