package com.github.sebersole.gradle.quarkus.extension;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.Services;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.StrictModuleIdentifierComparator;
import com.github.sebersole.gradle.quarkus.orm.HibernateOrmExtension;

import static com.github.sebersole.gradle.quarkus.Helper.DEPLOYMENT_ARTIFACT_KEY;

/**
 * Service for handling Quarkus extensions
 */
public class ExtensionService {
	private final Services services;
	private final QuarkusSpec quarkusDsl;
	private final Project gradleProject;

	private final Map<ModuleVersionIdentifier, QuarkusExtension> availableExtensions = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

	private Map<ModuleVersionIdentifier, ResolvedExtension> resolvedExtensionsByModule;

	public ExtensionService(Services services, QuarkusSpec quarkusDsl, Project gradleProject) {
		this.services = services;
		this.quarkusDsl = quarkusDsl;
		this.gradleProject = gradleProject;
	}

	public void afterServicesInit() {
		Logging.LOGGER.trace( "ExtensionService#afterServicesInit" );
	}

	public void prepareForUse() {
		Logging.LOGGER.trace( "ExtensionService#prepareForUse" );
	}

	public void extensionResolved(ResolvedDependency resolvedDependency) {
		assert resolvedDependency.getExtensionProperties() != null;

		registerAvailableExtension( resolvedDependency );
	}

	public QuarkusExtension registerAvailableExtension(ResolvedDependency runtimeDependency) {
		final Properties extensionProperties = runtimeDependency.getExtensionProperties();
		assert extensionProperties != null;

		final QuarkusExtension descriptor = createDescriptor(
				new ExtensionDescriptorCreationState() {
					@Override
					public ResolvedDependency getRuntimeDependency() {
						return runtimeDependency;
					}

					@Override
					public ModuleVersionIdentifier getDeploymentDependencyIdentifier() {
						final String deploymentArtifactNotation = getRuntimeDependency().getExtensionProperties().getProperty( DEPLOYMENT_ARTIFACT_KEY );
						return Helper.moduleVersionIdentifier( deploymentArtifactNotation );
					}

					@Override
					public QuarkusSpec getQuarkusDsl() {
						return quarkusDsl;
					}

					@Override
					public Project getGradleProject() {
						return gradleProject;
					}
				}
		);

		availableExtensions.put( descriptor.getModuleIdentifier(), descriptor );

		descriptor.contribute(
				new ExtensionContributionState() {
					@Override
					public Services getServices() {
						return services;
					}

					@Override
					public QuarkusSpec getQuarkusDsl() {
						return quarkusDsl;
					}

					@Override
					public Project getGradleProject() {
						return gradleProject;
					}
				}
		);

		return descriptor;
	}

	public static QuarkusExtension createDescriptor(ExtensionDescriptorCreationState creationState) {
		final ModuleVersionIdentifier deploymentArtifactIdentifier = creationState.getDeploymentDependencyIdentifier();
		if ( deploymentArtifactIdentifier != null ) {
			if ( "quarkus-hibernate-orm-deployment".equals( deploymentArtifactIdentifier.getArtifactName() ) ) {
				// NOTE: this is simply a hack/mock showing what it might look like
				// to allow an extension to inject special handling in the script
				// (extended DSL, etc) and/or the process of Quarkus building (steps, etc)
				//
				// todo : in "real life" this would somehow be gleaned from the extension's deployment
				// 		artifact rather than hard-coding them here. discuss with the quarkus team.
				// 		The best option I can think of is for the deployment artifact of such extensions
				// 		to include an implementation of `ExtensionDescriptor` that we can discover
				// 		(Jandex, ServiceLoader, etc) and leverage `ExtensionDescriptor#contribute`

				return creationState.getGradleProject().getObjects().newInstance( HibernateOrmExtension.class, creationState );
			}
		}

		return creationState.getGradleProject().getObjects().newInstance( StandardQuarkusExtension.class, creationState );
	}

	public Map<ModuleVersionIdentifier, QuarkusExtension> getAvailableExtensions() {
		return availableExtensions;
	}

	public void prepareForProcessing() {
		if ( resolvedExtensionsByModule != null ) {
			throw new IllegalStateException( "Already resolved" );
		}

		resolvedExtensionsByModule = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

		final ExtensionResolutionProcess process = new ExtensionResolutionProcess(
				services,
				quarkusDsl,
				gradleProject,
				availableExtensions,
				resolvedExtensionsByModule
		);

		process.execute();
	}

	public ResolvedExtension findRegisteredExtensionByName(String extensionName) {
		for ( Map.Entry<ModuleVersionIdentifier, ResolvedExtension> entry : resolvedExtensionsByModule.entrySet() ) {
			final ResolvedExtension resolvedExtension = entry.getValue();

			if ( extensionName.equals( resolvedExtension.getDslName() ) ) {
				return resolvedExtension;
			}

			if ( extensionName.equals( resolvedExtension.getExtensionIdentifier().getArtifactName() ) ) {
				return resolvedExtension;
			}

			if ( extensionName.equals( resolvedExtension.getExtensionIdentifier().groupArtifact() ) ) {
				return resolvedExtension;
			}
		}

		return null;
	}

	public void visitResolvedExtension(BiConsumer<ModuleIdentifier,ResolvedExtension> consumer) {
		resolvedExtensionsByModule.forEach( consumer );
	}

	public ResolvedExtension findResolvedExtension(ModuleVersionIdentifier identifier) {
		return resolvedExtensionsByModule.get( identifier );
	}

	public ResolvedExtension locateResolvedExtension(
			ModuleVersionIdentifier identifier,
			Function<ModuleVersionIdentifier,ResolvedExtension> creator) {
		final ResolvedExtension existing = resolvedExtensionsByModule.get( identifier );
		if ( existing != null ) {
			return existing;
		}

		final ResolvedExtension created = creator.apply( identifier );
		resolvedExtensionsByModule.put( identifier, created );
		return created;
	}

	private static class ExtensionResolutionProcess implements ExtensionResolutionState {
		private final Services services;
		private final QuarkusSpec quarkusDsl;
		private final Project gradleProject;

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// inputs
		private final Map<ModuleVersionIdentifier, QuarkusExtension> availableExtensions;


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// outputs
		private final Map<ModuleVersionIdentifier, ResolvedExtension> extensionsByModule;


		public ExtensionResolutionProcess(
				Services services,
				QuarkusSpec quarkusDsl,
				Project gradleProject,
				Map<ModuleVersionIdentifier, QuarkusExtension> availableExtensions,
				Map<ModuleVersionIdentifier, ResolvedExtension> extensionsByModule) {
			this.services = services;
			this.quarkusDsl = quarkusDsl;
			this.gradleProject = gradleProject;

			this.availableExtensions = availableExtensions;

			this.extensionsByModule = extensionsByModule;
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// ExtensionResolutionState


		@Override
		public QuarkusSpec getQuarkusDsl() {
			return quarkusDsl;
		}

		@Override
		public Project getGradleProject() {
			return gradleProject;
		}

		@Override
		public Services getServices() {
			return services;
		}

		private void execute() {
			final HashSet<ModuleVersionIdentifier> unconfiguredExtensions = new HashSet<>( availableExtensions.keySet() );

			final Iterator<ModuleVersionIdentifier> iterator = unconfiguredExtensions.iterator();
			while ( iterator.hasNext() ) {
				final ModuleIdentifier identifier = iterator.next();
				final QuarkusExtension quarkusExtension = availableExtensions.get( identifier );
				internalRegisterExtension( quarkusExtension );
				iterator.remove();
			}

			assert unconfiguredExtensions.isEmpty();
		}

		private ResolvedExtension internalRegisterExtension(QuarkusExtension extensionDescriptor) {
			final ResolvedDependency runtimeDependency = extensionDescriptor.getRuntimeDependency();
			final ModuleIdentifier identifier = runtimeDependency.getIdentifier();

			final ResolvedExtension existing = extensionsByModule.get( identifier );
			if ( existing != null ) {
				return existing;
			}

			final ResolvedExtension resolvedExtension = extensionDescriptor.resolve( this );
			services.getBuildDetails().getRuntimeDependencies().extendsFrom( resolvedExtension.getRuntimeDependencies() );
			services.getBuildDetails().getDeploymentDependencies().extendsFrom( resolvedExtension.getDeploymentDependencies() );

			extensionsByModule.put( resolvedExtension.getExtensionIdentifier(), resolvedExtension );

			resolveDependencies( resolvedExtension );

			return resolvedExtension;

		}

		public void resolveDependencies(ResolvedExtension extension) {
			resolveRuntimeDependencies( extension );
		}

		private void resolveRuntimeDependencies(ResolvedExtension extension) {
			Logging.LOGGER.debug( "Resolving runtime dependencies for `{}` extension", extension.getDslName() );

			// visit each runtime dependency for the artifact...
			extension.getRuntimeDependencies().getResolvedConfiguration().getResolvedArtifacts().forEach(
					resolvedArtifact -> {
						final ModuleVersionIdentifier artifactIdentifier = new StandardModuleVersionIdentifier( resolvedArtifact );

						if ( artifactIdentifier.groupArtifact().equals( extension.getExtensionIdentifier().groupArtifact() ) ) {
							return;
						}

						services.getArtifactService().resolveDependency( artifactIdentifier, resolvedArtifact );
					}
			);
		}
	}
}
