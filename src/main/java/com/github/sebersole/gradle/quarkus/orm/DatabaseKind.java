package com.github.sebersole.gradle.quarkus.orm;

import java.util.Locale;

import com.github.sebersole.gradle.quarkus.artifacts.ModuleIdentifier;

import static com.github.sebersole.gradle.quarkus.artifacts.ArtifactService.QUARKUS_GROUP;

/**
 * @author Steve Ebersole
 */
public enum DatabaseKind implements ModuleIdentifier {
	DERBY( "derby" ),
	H2( "h2" );

	private final String simpleName;
	private final String artifactName;
	private final String jdbcUrlProtocol;

	DatabaseKind(String simpleName) {
		this.simpleName = simpleName;
		this.artifactName = "quarkus-jdbc-" + simpleName;
		this.jdbcUrlProtocol = "jdbc:" + simpleName + ":";
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getJdbcUrlProtocol() {
		return jdbcUrlProtocol;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ModuleIdentifier

	@Override
	public String getGroupName() {
		return QUARKUS_GROUP;
	}

	@Override
	public String getArtifactName() {
		return artifactName;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// static resolvers

	public static DatabaseKind fromName(String name) {
		if ( DERBY.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
			return DERBY;
		}

		if ( H2.name().equals( name.toUpperCase( Locale.ROOT ) ) ) {
			return H2;
		}

		return null;
	}

	public static DatabaseKind extractFromUrl(String url) {
		final DatabaseKind[] families = DatabaseKind.values();
		//noinspection ForLoopReplaceableByForEach
		for ( int i = 0; i < families.length; i++ ) {
			if ( url.startsWith( families[ i ].jdbcUrlProtocol ) ) {
				return families[ i ];
			}
		}

		return null;
	}
}
