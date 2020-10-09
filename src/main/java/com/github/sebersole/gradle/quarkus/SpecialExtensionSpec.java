package com.github.sebersole.gradle.quarkus;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * @author Steve Ebersole
 */
public class SpecialExtensionSpec implements ExtensionSpec {
	private final String name;
	private final Property<String> runtimeArtifact;
	private final Property<String> specialValue;

	@Inject
	public SpecialExtensionSpec(String name, ObjectFactory objectFactory) {
		this.name = name;
		this.runtimeArtifact = objectFactory.property( String.class );
		this.specialValue = objectFactory.property( String.class );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Property<String> getRuntimeArtifact() {
		return runtimeArtifact;
	}

	public Property<String> getSpecialValue() {
		return specialValue;
	}

	public void setSpecialValue(String value) {
		getSpecialValue().set( value );
	}
}
