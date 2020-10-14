package com.github.sebersole.gradle.quarkus.artifacts;

import java.util.Properties;

import com.github.sebersole.gradle.quarkus.jandex.IndexCreator;

/**
 * A resolved dependency keeping track of information we need
 */
public interface ResolvedDependency extends ModuleVersionIdentifierAccess {
	@Override
	default ModuleVersionIdentifier getModuleVersionIdentifier() {
		return getIdentifier();
	}

	@Override
	default ModuleVersionIdentifier getModuleIdentifier() {
		return getIdentifier();
	}

	ModuleVersionIdentifier getIdentifier();

	Properties getExtensionProperties();
	IndexCreator getIndexCreator();
}
