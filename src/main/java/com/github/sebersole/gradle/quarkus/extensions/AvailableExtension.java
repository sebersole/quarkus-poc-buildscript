package com.github.sebersole.gradle.quarkus.extensions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedArtifact;

import com.github.sebersole.gradle.quarkus.ExtensionSpec;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * @author Steve Ebersole
 */
@FunctionalInterface
public interface AvailableExtension<T extends ExtensionSpec> {
	static <X extends ExtensionSpec, E extends AvailableExtension<X>> E from(ResolvedArtifact artifact, Project project) {
		if ( "quarkus-hibernate-orm".equals( artifact.getName() ) ) {
			return (E) project.getObjects().newInstance( HibernateOrmExtension.class );
		}

		return (E) project.getObjects().newInstance( StandardExtension.class );
	}

	void contribute(QuarkusSpec quarkusSpec, Project gradleProject);
}
