package com.github.sebersole.gradle.quarkus.extensions;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import com.github.sebersole.gradle.quarkus.ExtensionSpec;

/**
 * @author Steve Ebersole
 */
public class HibernateOrmExtensionSpec implements ExtensionSpec {
	private final String name;

	private final Property<String> databaseKind;
	private final Property<String> runtimeArtifact;

	@Inject
	public HibernateOrmExtensionSpec(String name, ObjectFactory objectFactory) {
		this.name = name;

		this.runtimeArtifact = objectFactory.property( String.class );
		this.runtimeArtifact.set( HibernateOrmExtension.ARTIFACT_NAME );

		this.databaseKind = objectFactory.property( String.class );
		// I think I read that `h2` is the default...
		// 		- anyway, let's illustrate "property defaults" for the moment
		//		- though this should also consider the `application.properties` value if there is one
		//	todo : wire in `BuildDetails#getApplicationProperty` when that gets pulled over
		//			- see `HibernateOrmExtension#DB_KIND_PROP_KEY` & `#JDBC_URL_PROP_KEY`
		this.databaseKind.convention( "h2" );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Property<String> getRuntimeArtifact() {
		return runtimeArtifact;
	}

	public void setRuntimeArtifact(String notation) {
		runtimeArtifact.set( notation );
	}

	public Property<String> getDatabaseKind() {
		return databaseKind;
	}

	public void setDatabaseKind(String kind) {
		databaseKind.set( kind );
	}
}
