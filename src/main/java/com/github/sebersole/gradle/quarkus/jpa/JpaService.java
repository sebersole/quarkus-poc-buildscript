package com.github.sebersole.gradle.quarkus.jpa;

import java.util.Map;
import java.util.function.Consumer;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.datasource.DataSourceService;
import com.github.sebersole.gradle.quarkus.datasource.ResolveDataSourcesTask;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.service.Service;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * Service for JPA persistence-units
 */
public class JpaService implements Service<JpaService> {

	public static JpaService maybeRegister(ExtensionContributionState contributionState) {
		DataSourceService.maybeRegister( contributionState );

		final ResolveDataSourcesTask dsTask = (ResolveDataSourcesTask) contributionState
				.getGradleProject()
				.getTasks()
				.getByName( ResolveDataSourcesTask.DSL_NAME );

		final JpaService existing = contributionState.getServices().findService( JpaService.class );
		if ( existing != null ) {
			return existing;
		}

		final JpaService jpaService = new JpaService( contributionState.getQuarkusDsl(), contributionState.getServices() );
		contributionState.getServices().registerService( jpaService );

		final JpaSpec existingDsl = contributionState.getQuarkusDsl().getExtensions().findByType( JpaSpec.class );
		if ( existingDsl == null ) {
			contributionState.getQuarkusDsl().getExtensions().create( JpaSpec.DSL_NAME, JpaSpec.class, contributionState );
		}
		else {
			contributionState.getGradleProject().getLogger().warn( "Registering JpaService encountered existing JpaSpec DSL extension" );
		}

		final ResolveJpaTask unitsTask = ResolveJpaTask.apply(
				contributionState.getQuarkusDsl(),
				contributionState.getServices(),
				contributionState.getGradleProject()
		);

		unitsTask.dependsOn( dsTask );

		return jpaService;
	}

	private final QuarkusSpec quarkusDsl;
	private final Services services;

	private Map<String,PersistenceUnit> persistenceUnits;

	public JpaService(QuarkusSpec quarkusDsl, Services services) {
		this.quarkusDsl = quarkusDsl;
		this.services = services;
	}

	public void resolve() {
		final JpaSpec jpaSpec = quarkusDsl.getExtensions().findByType( JpaSpec.class );
		assert jpaSpec != null;

		persistenceUnits = PersistenceUnitResolver.from( jpaSpec.getPersistenceUnitContainer(), services );
	}

	@Override
	public Class<JpaService> getRole() {
		return JpaService.class;
	}

	public void forEach(Consumer<PersistenceUnit> consumer) {
		persistenceUnits.forEach( (name, pu) -> consumer.accept( pu ) );
	}
}
