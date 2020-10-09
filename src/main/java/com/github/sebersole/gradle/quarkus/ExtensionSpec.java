package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;

/**
 * @author Steve Ebersole
 */
public interface ExtensionSpec extends Named {
	Property<String> getRuntimeArtifact();

	default void setRuntimeArtifact(String notation) {
		getRuntimeArtifact().set( notation );
	}
}
