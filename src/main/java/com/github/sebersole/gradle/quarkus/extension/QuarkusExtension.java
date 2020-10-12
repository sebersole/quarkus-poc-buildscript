package com.github.sebersole.gradle.quarkus.extension;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifierAccess;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifierAccess;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;

/**
 * Descriptor for defined extensions.  Allows the extension to adjust the DSL / Project as needed
 *
 * @implNote Implementations should define a constructor which:<ol>
 *     <li>is annotated with `@javax.inject.Inject`</li>
 *     <li>defines *at least* one argument accepting {@link ExtensionDescriptorCreationState}</li>
 *     <li>may define additional arguments following Gradle's "service injection" paradigm</li>
 * </ol>
 *
 * todo : would it be better to have a single ExtensionDescriptor implementation and have it
 * 		figure out what to do based on the contents of the runtime/deployment artifacts?
 * 		+
 * 		e.g., locate an `ExtensionContributor` impl, `ExtensionResolver` impl, etc
 */
public interface QuarkusExtension extends ModuleVersionIdentifierAccess {

	ResolvedDependency getRuntimeDependency();

	@Override
	default ModuleVersionIdentifier getModuleVersionIdentifier() {
		return getRuntimeDependency().getModuleIdentifier();
	}

	@Override
	default ModuleVersionIdentifier getModuleIdentifier() {
		return getRuntimeDependency().getModuleIdentifier();
	}

	ModuleIdentifier getDeploymentDependencyIdentifier();

	void contribute(ExtensionContributionState creationState);

	ResolvedExtension resolve(ExtensionResolutionState resolutionState);
}
