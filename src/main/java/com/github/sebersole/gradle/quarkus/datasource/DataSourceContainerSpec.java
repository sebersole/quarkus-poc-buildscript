package com.github.sebersole.gradle.quarkus.datasource;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.DslExtensionSpec;
import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.artifacts.ArtifactService;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.artifacts.ResolvedDependency;
import com.github.sebersole.gradle.quarkus.artifacts.StandardModuleVersionIdentifier;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionResolutionState;
import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.extension.QuarkusExtension;

/**
 * DSL extension for configuration of the Quarkus data-sources
 */
public class DataSourceContainerSpec implements DslExtensionSpec  {
	public static final String DSL_NAME = "dataSources";
	public static final String DEFAULT_DS_NAME = "{default}";

	public static DataSourceContainerSpec maybeRegister(ExtensionContributionState contributionState) {
		// other extensions might conceivably register JPA extension
		final DataSourceContainerSpec existing = contributionState.getQuarkusDsl().getExtensions().findByType( DataSourceContainerSpec.class );
		if ( existing != null ) {
			return existing;
		}

		return contributionState.getQuarkusDsl().getExtensions().create( DataSourceContainerSpec.DSL_NAME, DataSourceContainerSpec.class, contributionState );
	}

	private final Project gradleProject;
	private final Services services;

	private final NamedDomainObjectContainer<DataSourceSpec> dataSourceContainer;

	public DataSourceContainerSpec(ExtensionContributionState contributionState) {
		gradleProject = contributionState.getGradleProject();
		services = contributionState.getServices();
		dataSourceContainer = gradleProject.getObjects().domainObjectContainer( DataSourceSpec.class );
	}

	public DataSourceSpec findDefaultDataSourceSpec() {
		return dataSourceContainer.findByName( DEFAULT_DS_NAME );
	}

	public DataSourceSpec getDefaultDataSourceSpec() {
		final DataSourceSpec found = findDefaultDataSourceSpec();
		if ( found == null ) {
			throw new IllegalStateException( "No default data-source defined" );
		}
		return found;
	}

	public DataSourceSpec getOrMakeDefaultDataSourceSpec() {
		final DataSourceSpec found = findDefaultDataSourceSpec();
		if ( found != null ) {
			return found;
		}

		final DataSourceSpec created = new DataSourceSpec( DEFAULT_DS_NAME, gradleProject, services );
		dataSourceContainer.add( created );
		return created;
	}

	public void setDatabaseKind(String databaseKind) {
		// this triggers usage of the legacy notion of a default PU
		getOrMakeDefaultDataSourceSpec().setDatabaseKind( databaseKind );
	}

	public void dataSources(Action<NamedDomainObjectContainer<DataSourceSpec>> action) {
		action.execute( dataSourceContainer );
	}

	public NamedDomainObjectContainer<DataSourceSpec> getDataSourceContainer() {
		return dataSourceContainer;
	}

	@Override
	public String getDisplayInfo() {
		final StringBuilder buffer = new StringBuilder( "dataSources: [ " );
		dataSourceContainer.forEach(
				dataSourceSpec -> buffer.append( dataSourceSpec.toString() ).append( "`, " )
		);
		return buffer.append( " ]" ).toString();
	}

	private boolean databaseKindsApplied;

	public void applyDatabaseKinds(ExtensionResolutionState resolutionState) {
		if ( databaseKindsApplied ) {
			return;
		}

		databaseKindsApplied = true;

		final String quarkusVersion = resolutionState.getServices().getBuildDetails().getQuarkusVersion().get();

		if ( dataSourceContainer.isEmpty() ) {
			resolutionState.getGradleProject().getLogger().lifecycle( "Applying default data-source as none were explicitly configured" );
			applyDatabaseKind( DatabaseKind.H2, quarkusVersion, resolutionState );
		}
		else {
			dataSourceContainer.forEach(
					dataSourceSpec -> applyDatabaseKind( dataSourceSpec.getDatabaseKind().get(), quarkusVersion, resolutionState )
			);
		}
	}

	private static void applyDatabaseKind(
			DatabaseKind databaseKind,
			String version,
			ExtensionResolutionState resolutionState) {
		final ModuleVersionIdentifier dbKindExtensionId = new StandardModuleVersionIdentifier(
				databaseKind.getGroupName(),
				databaseKind.getArtifactName(),
				version
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
