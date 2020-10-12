package com.github.sebersole.gradle.quarkus.orm;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import com.github.sebersole.gradle.quarkus.BuildDetails;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.DslExtensionSpec;

import static com.github.sebersole.gradle.quarkus.orm.HibernateOrmExtension.DB_KIND_PROP_KEY;
import static com.github.sebersole.gradle.quarkus.orm.HibernateOrmExtension.JDBC_URL_PROP_KEY;

/**
 * DSL extension (Gradle sense) for configuring the hibernate-orm extension (Quarkus sense)
 */
public class HibernateOrmSpec implements DslExtensionSpec {
	private final Property<DatabaseKind> databaseKind;

	@SuppressWarnings( "UnstableApiUsage" )
	@Inject
	public HibernateOrmSpec(ExtensionContributionState contributionState) {
		final ObjectFactory objectFactory = contributionState.getGradleProject().getObjects();
		this.databaseKind = objectFactory.property( DatabaseKind.class );

		final BuildDetails buildDetails = contributionState.getServices().getBuildDetails();
		// Gradle feature.. we are setting a "Supplier" for this property's value.  Later
		// access will trigger this block if no explicit value has been set
		this.databaseKind.convention(
				contributionState.getGradleProject().provider(
						() -> buildDetails.getApplicationProperty(
								DB_KIND_PROP_KEY,
								DatabaseKind::fromName,
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
						)
				)
		);
	}

	public Property<DatabaseKind> getDatabaseKind() {
		return databaseKind;
	}

	public void setDatabaseKind(String kind) {
		databaseKind.set( DatabaseKind.fromName( kind ) );
	}

	@Override
	public String getDisplayInfo() {
		return "{ db-kind: " + databaseKind.get() + " }";
	}
}
