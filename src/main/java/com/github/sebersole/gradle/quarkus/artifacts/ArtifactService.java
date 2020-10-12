package com.github.sebersole.gradle.quarkus.artifacts;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Logging;
import com.github.sebersole.gradle.quarkus.ProjectInfo;
import com.github.sebersole.gradle.quarkus.Services;

/**
 * Cache of artifacts
 */
public class ArtifactService {
	public static final String QUARKUS_PLATFORMS = "quarkusPlatforms";
	public static final String QUARKUS_RUNTIME_DEPS = "quarkusRuntime";
	public static final String QUARKUS_DEPLOYMENT_DEPS = "quarkusDeployment";

	public static final String QUARKUS_GROUP = "io.quarkus";
	public static final String QUARKUS_BOM = "quarkus-bom";
	public static final String QUARKUS_UNIVERSE_COMMUNITY_BOM = "quarkus-universe-bom";

	private final Services services;
	private final Project project;

	// all artifacts
	private final Map<ModuleVersionIdentifier, ResolvedDependency> resolvedDependencies = new TreeMap<>( StrictModuleIdentifierComparator.INSTANCE );

	public ArtifactService(Services services, Project project) {
		this.services = services;
		this.project = project;
	}

	public void afterServicesInit() {
		Logging.LOGGER.trace( "ArtifactService#afterServicesInit" );

		final Configuration platforms = services.getBuildDetails().getPlatforms();

		final Configuration buildScriptClasspath = project.getBuildscript().getConfigurations().getByName( "classpath" );
		final ResolvedConfiguration resolvedBuildScriptClasspath = buildScriptClasspath.getResolvedConfiguration();
		final Set<ResolvedArtifact> resolvedArtifacts = resolvedBuildScriptClasspath.getResolvedArtifacts();

		resolvedBuildScriptClasspath.getFirstLevelModuleDependencies().forEach(
				resolvedDependency -> {
					// WARN: hackalious!!
					project.getLogger().debug( "Checking `{}` as platform", resolvedDependency.getModule().getId().toString() );
					if ( resolvedDependency.toString().endsWith( ";enforced-platform-runtime" ) ) {
						project.getLogger().debug( "  > is a platform" );

						// add it as a dependency to the platforms Configuration
						project.getDependencies().add( platforms.getName(), Helper.groupArtifactVersion( resolvedDependency ) );
					}
					else {
						project.getLogger().debug( "  > not a platform" );
					}
				}
		);

		resolvedArtifacts.forEach(
				resolvedArtifact -> {
					final ModuleVersionIdentifier identifier = new StandardModuleVersionIdentifier( resolvedArtifact );
					final ResolvedDependency resolvedDependency = resolveDependency(
							identifier,
							id -> makeDependencyReference( identifier, resolvedArtifact )
					);
					resolvedDependencies.put( identifier, resolvedDependency );
				}
		);
	}

	public void prepareForUse() {
		Logging.LOGGER.trace( "ArtifactService#prepareForUse" );
	}

	private ResolvedDependency makeDependencyReference(ModuleVersionIdentifier identifier, ResolvedArtifact resolvedArtifact) {
		final ResolvedDependency resolvedDependency = createDependencyReference( identifier, resolvedArtifact );

		if ( resolvedDependency.getExtensionProperties() != null ) {
			services.getExtensionService().extensionResolved( resolvedDependency );
		}

		return resolvedDependency;
	}

	private ResolvedDependency createDependencyReference(ModuleVersionIdentifier identifier, ResolvedArtifact resolvedArtifact) {
		final File artifactBase = resolvedArtifact.getFile();
		assert artifactBase.exists();

		// NOTE : we make the distinction between a project-dependency and an
		// external-dependency based on whether the artifact's File is a file or directory

		if ( artifactBase.isDirectory() ) {
			// we assume this is a project
			final ProjectInfo projectInfo = services.getProjectService().getProjectInfo( identifier );
			assert projectInfo != null;
			return new ProjectDependency( identifier, resolvedArtifact, projectInfo );
		}

		return new ExternalDependency( identifier, resolvedArtifact );
	}

	public ResolvedDependency resolveDependency(ModuleVersionIdentifier artifactIdentifier, ResolvedArtifact resolvedArtifact) {
		return resolveDependency(
				artifactIdentifier,
				identifier -> makeDependencyReference( identifier, resolvedArtifact )
		);
	}

	public ResolvedDependency resolveDependency(ModuleVersionIdentifier artifactIdentifier) {
		return resolveDependency(
				artifactIdentifier,
				identifier -> {
					// todo : apply platforms
					final Configuration dependencyConfig = project.getConfigurations().create( artifactIdentifier.getArtifactName() );

					project.getDependencies().add( dependencyConfig.getName(), artifactIdentifier.groupArtifactVersion() );

					final org.gradle.api.artifacts.ResolvedDependency gradleResolvedDependency = Helper.extractOnlyOne(
							dependencyConfig.getResolvedConfiguration().getFirstLevelModuleDependencies(),
							() -> null,
							() -> { throw new IllegalStateException( "Expecting just a single `org.gradle.api.artifacts.ResolvedDependency`" ); }
					);

					assert gradleResolvedDependency != null;

					final ResolvedArtifact gradleResolvedArtifact = Helper.extractOnlyOne(
							gradleResolvedDependency.getModuleArtifacts(),
							() -> null,
							() -> { throw new IllegalStateException( "Expecting just a single `org.gradle.api.artifacts.ResolvedArtifact`" ); }
					);
					assert gradleResolvedArtifact != null;

					return makeDependencyReference( artifactIdentifier, gradleResolvedArtifact );
				}
		);
	}

	public ResolvedDependency resolveDependency(
			ModuleVersionIdentifier identifier,
			Function<ModuleVersionIdentifier,ResolvedDependency> creator) {
		final ResolvedDependency existing = resolvedDependencies.get( identifier );
		if ( existing != null ) {
			return existing;
		}

		final ResolvedDependency resolvedDependency = creator.apply( identifier );
		resolvedDependencies.put( identifier, resolvedDependency );
		return resolvedDependency;
	}

}
