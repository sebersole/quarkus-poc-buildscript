package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * State accessible to the creation of an `ExtensionDescriptor`
 */
public interface ExtensionDescriptorCreationState {
	/**
	 * ResolvedDependency descriptor for the extension's runtime artifact
	 */
	ResolvedDependency getRuntimeDependency();

	/**
	 * The identifier for the dependency that is the deployment artifact
	 * for the extension
	 *
	 * todo : Optional?  it is nulllable
	 */
	ModuleVersionIdentifier getDeploymentDependencyIdentifier();

	QuarkusSpec getQuarkusDsl();

	Project getGradleProject();
}
