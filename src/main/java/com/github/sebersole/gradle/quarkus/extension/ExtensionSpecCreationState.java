package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * State accessible to ExtensionSpec creation
 */
public interface ExtensionSpecCreationState {
	ModuleVersionIdentifier getExtensionIdentifier();
	QuarkusSpec getQuarkusDsl();
	Project getGradleProject();
}
