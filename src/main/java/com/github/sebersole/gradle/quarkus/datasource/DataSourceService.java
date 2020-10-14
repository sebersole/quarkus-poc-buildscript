package com.github.sebersole.gradle.quarkus.datasource;

import java.util.Map;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * @author Steve Ebersole
 */
public class DataSourceService implements Service<DataSourceService> {

	public static DataSourceService maybeRegister(ExtensionContributionState contributionState) {
		final Services services = contributionState.getServices();
		final DataSourceService existing = services.findService( DataSourceService.class );
		if ( existing != null ) {
			return existing;
		}

		final DataSourceContainerSpec existingDsl = contributionState.getQuarkusDsl().getExtensions().findByType( DataSourceContainerSpec.class );
		if ( existingDsl == null ) {
			contributionState.getQuarkusDsl().getExtensions().create( DataSourceContainerSpec.DSL_NAME, DataSourceContainerSpec.class, contributionState );
		}

		final DataSourceService service = new DataSourceService( contributionState.getQuarkusDsl(), services );
		services.registerService( service );

		ResolveDataSourcesTask.apply( contributionState );

		return service;
	}

	private final QuarkusSpec quarkusDsl;
	private final Services services;

	private Map<String,DataSource> dataSourceMap;

	public DataSourceService(QuarkusSpec quarkusDsl, Services services) {
		this.quarkusDsl = quarkusDsl;
		this.services = services;
	}

	@Override
	public Class<DataSourceService> getRole() {
		return DataSourceService.class;
	}

	public void resolve() {

	}
}
