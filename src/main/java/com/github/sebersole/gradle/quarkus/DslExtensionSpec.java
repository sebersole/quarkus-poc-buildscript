package com.github.sebersole.gradle.quarkus;

/**
 * Marker interface for DSL extensions configuring a part of Quarkus
 *
 * @apiNote This is not necessarily 1-1 with a Quarkus extension...
 */
public interface DslExtensionSpec {
	String getDisplayInfo();
}
