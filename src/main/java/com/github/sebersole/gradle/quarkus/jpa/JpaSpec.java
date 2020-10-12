package com.github.sebersole.gradle.quarkus.jpa;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import com.github.sebersole.gradle.quarkus.DslExtensionSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;

/**
 * DSL extension for configuring JPA persistence-units
 */
public class JpaSpec implements DslExtensionSpec, ExtensionAware {
	public static final String DSL_NAME = "jpa";

	// todo : other properties?

	// todo : add JpaSpec-level definition of a singular persistence-unit to support legacy Gradle plugin

	private final NamedDomainObjectContainer<PersistenceUnitSpec> persistenceUnits;

	public JpaSpec(ExtensionContributionState contributionState) {
		persistenceUnits = contributionState.getGradleProject().getObjects().domainObjectContainer( PersistenceUnitSpec.class );
	}

	public void persistenceUnits(Action<NamedDomainObjectContainer<PersistenceUnitSpec>> action) {
		action.execute( persistenceUnits );
	}

	@Override
	public String getDisplayInfo() {
		return "{ persistenceUnits: { } }";
	}

	@Override
	public ExtensionContainer getExtensions() {
		throw new UnsupportedOperationException( "Gradle should implement this in its decoration of this instance" );
	}
}
