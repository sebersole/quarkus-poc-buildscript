package com.github.sebersole.gradle.quarkus.jpa;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import com.github.sebersole.gradle.quarkus.DslExtensionSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;

/**
 * DSL extension for configuring JPA persistence-units
 */
public class JpaSpec implements DslExtensionSpec {
	public static final String DSL_NAME = "jpa";
	public static final String DEFAULT_PU_NAME = "default";

	// todo : other properties?

	// todo : add JpaSpec-level definition of a singular persistence-unit to support legacy Gradle plugin

	private final NamedDomainObjectContainer<PersistenceUnitSpec> persistenceUnits;

	public JpaSpec(ExtensionContributionState contributionState) {
		final ObjectFactory objectFactory = contributionState.getGradleProject().getObjects();
		persistenceUnits = objectFactory.domainObjectContainer(
				PersistenceUnitSpec.class,
				name -> objectFactory.newInstance( PersistenceUnitSpec.class, name, contributionState )
		);
	}

	public static void maybeRegister(ExtensionContributionState contributionState) {
		// other extensions might conceivably register JPA extension
		final JpaSpec existing = contributionState.getQuarkusDsl().getExtensions().findByType( JpaSpec.class );
		if ( existing == null ) {
			contributionState.getQuarkusDsl().getExtensions().create( JpaSpec.DSL_NAME, JpaSpec.class, contributionState );
		}
	}

	public void persistenceUnits(Action<NamedDomainObjectContainer<PersistenceUnitSpec>> action) {
		action.execute( persistenceUnits );
	}

	public NamedDomainObjectContainer<PersistenceUnitSpec> getPersistenceUnitContainer() {
		return persistenceUnits;
	}

	@Override
	public String getDisplayInfo() {
		final StringBuilder buffer = new StringBuilder( "persistenceUnits: [ ");
		persistenceUnits.forEach(
				persistenceUnitSpec -> buffer.append( persistenceUnitSpec.getUnitName() ).append( ", " )
		);
		return buffer.append( " ]" ).toString();
	}
}
