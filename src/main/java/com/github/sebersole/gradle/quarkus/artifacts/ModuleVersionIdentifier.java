package com.github.sebersole.gradle.quarkus.artifacts;

import java.util.Locale;

import com.github.sebersole.gradle.quarkus.Helper;

/**
 * Unique identifier for a module dependency version
 */
public interface ModuleVersionIdentifier extends ModuleIdentifier {
	/**
	 * The dependency's version
	 */
	String getVersion();

	default String groupArtifactVersion() {
		return Helper.groupArtifactVersion( getGroupName(), getArtifactName(), getVersion() );
	}
}
