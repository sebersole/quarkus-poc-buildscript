package com.github.sebersole.gradle.quarkus.extensions;

import java.util.Iterator;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * "Mock" of a deployment-contributed extension special handling
 */
public class HibernateOrmExtension implements AvailableExtension<HibernateOrmExtensionSpec> {
	public static final String ARTIFACT_NAME = Helper.QUARKUS + "-hibernate-orm";
	public static final String DB_KIND_PROP_KEY = "quarkus.datasource.db-kind";
	public static final String JDBC_URL_PROP_KEY = "quarkus.datasource.jdbc.url";

	public HibernateOrmExtension() {
	}

	@Override
	public Class<HibernateOrmExtensionSpec> getExtensionClass() {
		return HibernateOrmExtensionSpec.class;
	}

	@Override
	public void contribute(QuarkusSpec quarkusSpec, Project gradleProject) {
		quarkusSpec.getExtensionSpecContainer().registerFactory(
				HibernateOrmExtensionSpec.class,
				new SpecFactory( quarkusSpec, gradleProject )
		);
	}

	private static class SpecFactory implements NamedDomainObjectFactory<HibernateOrmExtensionSpec> {
		private final QuarkusSpec quarkusSpec;
		private final Project gradleProject;

		private SpecFactory(QuarkusSpec quarkusSpec, Project gradleProject) {
			this.quarkusSpec = quarkusSpec;
			this.gradleProject = gradleProject;
		}

		@Override
		public HibernateOrmExtensionSpec create(String name) {
			// first see if there is already one registered - there should only ever be one of these
			// todo : that's maybe generally true of all extensions?

			final Iterator<HibernateOrmExtensionSpec> iterator = quarkusSpec.getExtensionSpecContainer().withType( HibernateOrmExtensionSpec.class ).iterator();
			if ( iterator.hasNext() ) {
				final HibernateOrmExtensionSpec previous = iterator.next();
				if ( iterator.hasNext() || ! previous.getName().equals( name ) ) {
					throw new GradleException( "Multiple registrations of HibernateOrmExtensionSpec found" );
				}
			}

			// its ctor sets up its own state
			return gradleProject.getObjects().newInstance( HibernateOrmExtensionSpec.class, name );
		}
	}
}
