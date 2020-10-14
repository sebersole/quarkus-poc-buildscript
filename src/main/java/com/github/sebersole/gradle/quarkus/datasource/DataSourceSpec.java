package com.github.sebersole.gradle.quarkus.datasource;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import com.github.sebersole.gradle.quarkus.BuildDetails;
import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;

/**
 * DSL extension for configuration of a Quarkus data-source
 */
public class DataSourceSpec implements Named {
	public static final String DS_PROP_KEY_BASE = "quarkus.datasource";
	public static final String DS_DB_KIND_PROP_NAME = "db-kind";
	public static final String DS_URL_PROP_NAME = "jdbc-url";
	public static final String DB_KIND_PROP_KEY = DS_PROP_KEY_BASE + "." + DS_DB_KIND_PROP_NAME;
	public static final String JDBC_URL_PROP_KEY = DS_PROP_KEY_BASE + "." + DS_URL_PROP_NAME;

	private final String dataSourceName;
	private final Property<DatabaseKind> databaseKind;

	private final BuildDetails buildDetails;

	@SuppressWarnings( "UnstableApiUsage" )
	public DataSourceSpec(String dataSourceName, ExtensionContributionState contributionState) {
		this( dataSourceName, contributionState.getGradleProject(), contributionState.getServices() );
	}

	public DataSourceSpec(String dataSourceName, Project gradleProject, Services services) {
		this.dataSourceName = dataSourceName;

		buildDetails = services.getBuildDetails();

		databaseKind = gradleProject.getObjects().property( DatabaseKind.class );

		if ( DataSourceContainerSpec.DEFAULT_DS_NAME.equals( dataSourceName ) ) {
			databaseKind.convention( gradleProject.provider( this::defaultDataSourceDatabaseKind ) );
		}
		else {
			databaseKind.convention( gradleProject.provider( () -> namedDataSourceDatabaseKind( dataSourceName ) ) );
		}
	}

	private DatabaseKind defaultDataSourceDatabaseKind() {
		return buildDetails.getApplicationProperty(
				DB_KIND_PROP_KEY,
				DatabaseKind::fromProperty,
				dbKindKey -> {
					// there was no `db-kind` specified in the `application.properties`.  look at the jdbc url, if one
					final String jdbcUrl = buildDetails.getApplicationProperty( JDBC_URL_PROP_KEY );
					if ( jdbcUrl != null ) {
						final DatabaseKind fromUrl = DatabaseKind.extractFromUrl( jdbcUrl );
						if ( fromUrl != null ) {
							return fromUrl;
						}
					}

					// I think I read that `h2` is the default...
					return DatabaseKind.H2;
				}
		);
	}

	private DatabaseKind namedDataSourceDatabaseKind(String dataSourceName) {
		final String NAMED_BASE = DS_PROP_KEY_BASE + "." + dataSourceName;


		return buildDetails.getApplicationProperty(
				NAMED_BASE + "." + DS_DB_KIND_PROP_NAME,
				DatabaseKind::fromProperty,
				dbKindKey -> {
					// there was no `db-kind` specified in the `application.properties`.  look at the jdbc url, if one
					final String jdbcUrl = buildDetails.getApplicationProperty( NAMED_BASE + "." + DS_URL_PROP_NAME );
					if ( jdbcUrl != null ) {
						final DatabaseKind fromUrl = DatabaseKind.extractFromUrl( jdbcUrl );
						if ( fromUrl != null ) {
							return fromUrl;
						}
					}

					// I think I read that `h2` is the default...
					return DatabaseKind.H2;
				}
		);
	}

	@Override
	public String getName() {
		return dataSourceName;
	}

	public Property<DatabaseKind> getDatabaseKind() {
		return databaseKind;
	}

	public void setDatabaseKind(String kind) {
		databaseKind.set( DatabaseKind.fromName( kind ) );
	}

	@Override
	public String toString() {
		return dataSourceName + ": `" + databaseKind.get().name() + "`";
	}
}
