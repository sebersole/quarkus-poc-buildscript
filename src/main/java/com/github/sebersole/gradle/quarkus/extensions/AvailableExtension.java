package com.github.sebersole.gradle.quarkus.extensions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.ExtensionSpec;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * Descriptor for defined extensions.  Allows the extension to adjust the DSL / Project as needed
 */
public interface AvailableExtension<T extends ExtensionSpec> {
	static <X extends ExtensionSpec, E extends AvailableExtension<X>> E from(ResolvedArtifact artifact, Project project) {
		if ( "quarkus-hibernate-orm".equals( artifact.getName() ) ) {
			//noinspection unchecked
			return (E) project.getObjects().newInstance( HibernateOrmExtension.class );
		}

		//noinspection unchecked
		return (E) project.getObjects().newInstance( StandardExtension.class );
	}

	Class<T> getExtensionClass();

	void contribute(QuarkusSpec quarkusSpec, Project gradleProject);
}
