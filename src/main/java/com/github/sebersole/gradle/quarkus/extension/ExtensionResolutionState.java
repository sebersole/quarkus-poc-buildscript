package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * @author Steve Ebersole
 */
public interface ExtensionResolutionState {
	QuarkusSpec getQuarkusDsl();
	Project getGradleProject();
	Services getServices();
}
