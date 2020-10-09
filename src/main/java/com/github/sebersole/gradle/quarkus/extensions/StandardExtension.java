package com.github.sebersole.gradle.quarkus.extensions;

import javax.inject.Inject;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;

/**
 * @author Steve Ebersole
 */
public class StandardExtension implements AvailableExtension<StandardExtensionSpec> {
	private final NamedDomainObjectFactory<StandardExtensionSpec> factory;

	@Inject
	public StandardExtension(ObjectFactory objectFactory) {
		factory = (name) -> objectFactory.newInstance( StandardExtensionSpec.class, name );
	}

	@Override
	public Class<StandardExtensionSpec> getExtensionClass() {
		return StandardExtensionSpec.class;
	}

	@Override
	public void contribute(QuarkusSpec quarkusSpec, Project gradleProject) {
		// nothing to contribute - its NamedDomainObjectFactory has already been registered by the plugin
	}
}
