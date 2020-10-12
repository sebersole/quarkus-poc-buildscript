package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;

/**
 * @author Steve Ebersole
 */
public interface ResolvedExtension {
	/**
	 * The name used for this Extension in the Quarkus DSL extensions container
	 */
	String getDslName();

	ModuleVersionIdentifier getExtensionIdentifier();

	Configuration getRuntimeDependencies();
	Configuration getDeploymentDependencies();
}
