package com.github.sebersole.gradle.quarkus.orm;

import javax.inject.Inject;

import com.github.sebersole.gradle.quarkus.DslExtensionSpec;
import com.github.sebersole.gradle.quarkus.datasource.DataSourceContainerSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;

/**
 * DSL extension (Gradle sense) for configuring the hibernate-orm extension (Quarkus sense)
 */
public class HibernateOrmSpec implements DslExtensionSpec {
	@Inject
	public HibernateOrmSpec(ExtensionContributionState contributionState, DataSourceContainerSpec dataSourceContainerSpec) {
	}

	@Override
	public String getDisplayInfo() {
		return "{ }";
	}
}
