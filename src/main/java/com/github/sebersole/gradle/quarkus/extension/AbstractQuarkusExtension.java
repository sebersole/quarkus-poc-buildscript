package com.github.sebersole.gradle.quarkus.extension;

import javax.inject.Inject;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;

/**
 * Basic support for ExtensionDescriptor implementations
 */
public abstract class AbstractQuarkusExtension implements QuarkusExtension {
	private final ResolvedDependency runtimeDependency;
	private final ModuleVersionIdentifier deploymentDependencyIdentifier;

	@Inject
	public AbstractQuarkusExtension(ExtensionDescriptorCreationState creationState) {
		this.runtimeDependency = creationState.getRuntimeDependency();
		this.deploymentDependencyIdentifier = creationState.getDeploymentDependencyIdentifier();
	}

	@Override
	public ResolvedDependency getRuntimeDependency() {
		return runtimeDependency;
	}

	@Override
	public ModuleIdentifier getDeploymentDependencyIdentifier() {
		return deploymentDependencyIdentifier;
	}

	protected Configuration makeRuntimeDependencies(ExtensionResolutionState resolutionState) {
		return makeRuntimeDependencies(
				getConfigNameBase(),
				runtimeDependency.getIdentifier(),
				resolutionState
		);
	}

	protected Configuration makeDeploymentDependencies(ExtensionResolutionState resolutionState) {
		return makeDeploymentDependencies(
				getConfigNameBase(),
				runtimeDependency.getIdentifier(),
				deploymentDependencyIdentifier,
				resolutionState
		);
	}

	protected String getConfigNameBase() {
		return getArtifactName();
	}

	protected static Configuration makeRuntimeDependencies(String configNameBase, ModuleVersionIdentifier identifier, ExtensionResolutionState resolutionState) {
		final Configuration dependencies = makeDependencyConfiguration( configNameBase, identifier, "Runtime", resolutionState );
		resolutionState.getGradleProject().getDependencies().add(
				dependencies.getName(),
				identifier.groupArtifactVersion()
		);
		return dependencies;
	}

	protected static Configuration makeDeploymentDependencies(
			String configNameBase,
			ModuleVersionIdentifier runtimeIdentifier,
			ModuleVersionIdentifier deploymentIdentifier,
			ExtensionResolutionState resolutionState) {
		final Configuration dependencies = makeDependencyConfiguration( configNameBase, runtimeIdentifier, "Deployment", resolutionState );
		if ( deploymentIdentifier != null ) {
			resolutionState.getGradleProject().getDependencies().add(
					dependencies.getName(),
					deploymentIdentifier.groupArtifactVersion()
			);
			return dependencies;
		}
		return dependencies;
	}

	protected static Configuration makeDependencyConfiguration(
			String configNameBase,
			ModuleIdentifier identifier,
			String type,
			ExtensionResolutionState resolutionState) {
		final Configuration dependencies = resolutionState.getGradleProject().getConfigurations().maybeCreate( configNameBase + type );
		dependencies.setDescription( type + " dependencies for the `" + identifier.getArtifactName() + "` extension" );
		dependencies.extendsFrom( resolutionState.getServices().getBuildDetails().getPlatforms() );
		return dependencies;
	}
}
