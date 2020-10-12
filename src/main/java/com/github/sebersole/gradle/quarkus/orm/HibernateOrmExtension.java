package com.github.sebersole.gradle.quarkus.orm;

import javax.inject.Inject;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.Services;
import com.github.sebersole.gradle.quarkus.artifacts.ArtifactService;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.extension.AbstractQuarkusExtension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionDescriptorCreationState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionResolutionState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.extension.QuarkusExtension;
import com.github.sebersole.gradle.quarkus.extension.ResolvedExtension;
import com.github.sebersole.gradle.quarkus.jpa.JpaSpec;

/**
 * "Mock" of a deployment-contributed extension special handling
 */
public class HibernateOrmExtension extends AbstractQuarkusExtension {
	public static final String ARTIFACT_NAME = Helper.QUARKUS + "-hibernate-orm";
	public static final String DB_KIND_PROP_KEY = "quarkus.datasource.db-kind";
	public static final String JDBC_URL_PROP_KEY = "quarkus.datasource.jdbc.url";

	public static final String DSL_NAME = "hibernateOrm";

	@Inject
	public HibernateOrmExtension(ExtensionDescriptorCreationState creationState) {
		super( creationState );
	}

	@Override
	protected String getConfigNameBase() {
		return DSL_NAME;
	}

	@Override
	public void contribute(ExtensionContributionState contributionState) {
		contributionState.getQuarkusDsl().getExtensions().create( DSL_NAME, HibernateOrmSpec.class, contributionState );
		contributionState.getQuarkusDsl().getExtensions().create( JpaSpec.DSL_NAME, JpaSpec.class, contributionState );
	}

	@Override
	public ResolvedExtension resolve(ExtensionResolutionState resolutionState) {
		final HibernateOrmSpec config = resolutionState.getQuarkusDsl().getExtensions().getByType( HibernateOrmSpec.class );
		final DatabaseKind databaseKind = config.getDatabaseKind().get();

		applyDatabaseKind( databaseKind, resolutionState );

		final Configuration runtimeDependencies = makeRuntimeDependencies( resolutionState );
		final Configuration deploymentDependencies = makeDeploymentDependencies( resolutionState );

		runtimeDependencies.resolve();
		deploymentDependencies.resolve();

		return new ResolvedExtension() {
			@Override
			public String getDslName() {
				return DSL_NAME;
			}

			@Override
			public ModuleVersionIdentifier getExtensionIdentifier() {
				return getRuntimeDependency().getIdentifier();
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

	private void applyDatabaseKind(DatabaseKind databaseKind, ExtensionResolutionState resolutionState) {
		final ModuleVersionIdentifier dbKindExtensionId = new StandardModuleVersionIdentifier(
				databaseKind.getGroupName(),
				databaseKind.getArtifactName(),
				// assume the same version
				getRuntimeDependency().getVersion()
		);

		final Services services = resolutionState.getServices();
		final ArtifactService artifactService = services.getArtifactService();
		final ExtensionService extensionService = services.getExtensionService();

		extensionService.locateResolvedExtension(
				dbKindExtensionId,
				identifier -> {
					final ResolvedDependency dbKindDependency = artifactService.resolveDependency( dbKindExtensionId );
					final QuarkusExtension dbKindExtension = extensionService.registerAvailableExtension( dbKindDependency );
					return dbKindExtension.resolve( resolutionState );
				}
		);
	}

}
