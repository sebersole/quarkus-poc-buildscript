package com.github.sebersole.gradle.quarkus.orm;

import javax.inject.Inject;

import org.gradle.api.artifacts.Configuration;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.datasource.DataSourceContainerSpec;
import com.github.sebersole.gradle.quarkus.extension.AbstractQuarkusExtension;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionDescriptorCreationState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionResolutionState;
import com.github.sebersole.gradle.quarkus.extension.ResolvedExtension;
import com.github.sebersole.gradle.quarkus.jpa.JpaService;
import com.github.sebersole.gradle.quarkus.jpa.JpaSpec;

/**
 * "Mock" of a deployment-contributed extension special handling
 */
public class HibernateOrmExtension extends AbstractQuarkusExtension {
	public static final String ARTIFACT_NAME = Helper.QUARKUS + "-hibernate-orm";

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
		JpaService.maybeRegister( contributionState );
		final DataSourceContainerSpec dataSourceContainerSpec = DataSourceContainerSpec.maybeRegister( contributionState );

		contributionState.getQuarkusDsl().getExtensions().create( DSL_NAME, HibernateOrmSpec.class, contributionState, dataSourceContainerSpec );
	}

	@Override
	public ResolvedExtension resolve(ExtensionResolutionState resolutionState) {
		final DataSourceContainerSpec dataSourceConfig = resolutionState.getQuarkusDsl().getExtensions().getByType( DataSourceContainerSpec.class );
		dataSourceConfig.applyDatabaseKinds( resolutionState );

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

}
