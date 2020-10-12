package com.github.sebersole.gradle.quarkus.extension;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.Services;

/**
 * State accessible to contribution process of a Quarkus extension
 */
public interface ExtensionContributionState {
	Services getServices();
	QuarkusSpec getQuarkusDsl();
	Project getGradleProject();
}
