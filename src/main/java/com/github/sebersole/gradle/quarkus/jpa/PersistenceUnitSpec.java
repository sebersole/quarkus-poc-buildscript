package com.github.sebersole.gradle.quarkus.jpa;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Property;

import com.github.sebersole.gradle.quarkus.datasource.DataSourceContainerSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import groovy.lang.Closure;

/**
 * @author Steve Ebersole
 */
public class PersistenceUnitSpec implements Named {
	private final String unitName;
	private final Project project;

	private final Property<String> dataSourceName;
	private final Configuration dependencies;

	@Inject
	public PersistenceUnitSpec(String unitName, ExtensionContributionState contributionState) {
		this.unitName = unitName;
		project = contributionState.getGradleProject();

		dataSourceName = project.getObjects().property( String.class );
		dataSourceName.convention( DataSourceContainerSpec.DEFAULT_DS_NAME );

		dependencies = project.getConfigurations().maybeCreate( determineConfigurationName( unitName ) );
		dependencies.setDescription( "Dependencies for the `" + unitName + "` JPA persistence-unit" );
	}

	private static String determineConfigurationName(String unitName) {
		return unitName + "PersistenceUnitDependencies";
	}

	@Override
	public String getName() {
		return getUnitName();
	}

	public String getUnitName() {
		return unitName;
	}

	public Configuration getDependencies() {
		return dependencies;
	}

	public Dependency include(Object notation) {
		return project.getDependencies().add( dependencies.getName(), notation );
	}

	public void include(Object notation, Closure<Dependency> closure) {
		project.getDependencies().add( dependencies.getName(), notation, closure );
	}

	public void include(Object notation, Action<Dependency> action) {
		action.execute( include( notation ) );
	}
}
