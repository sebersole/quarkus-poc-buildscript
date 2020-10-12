package com.github.sebersole.gradle.quarkus.extension;

import javax.inject.Inject;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;

/**
 * @author Steve Ebersole
 */
public class StandardQuarkusExtension extends AbstractQuarkusExtension {
	@Inject
	public StandardQuarkusExtension(ExtensionDescriptorCreationState creationState) {
		super( creationState );
	}

	@Override
	public void contribute(ExtensionContributionState contributionState) {
	}

	@Override
	public ResolvedExtension resolve(ExtensionResolutionState resolutionState) {
		final ResolvedDependency runtimeDependency = getRuntimeDependency();

		final Configuration runtimeDependencies = makeRuntimeDependencies( resolutionState );
		final Configuration deploymentDependencies = makeDeploymentDependencies( resolutionState );

		return new ResolvedExtension() {
			@Override
			public String getDslName() {
				return null;
			}

			@Override
			public ModuleVersionIdentifier getExtensionIdentifier() {
				return runtimeDependency.getIdentifier();
			}

			@Override
			public Configuration getRuntimeDependencies() {
				return runtimeDependencies;
			}

			@Override
			public Configuration getDeploymentDependencies() {
				return deploymentDependencies;
			}
		};
	}
}
