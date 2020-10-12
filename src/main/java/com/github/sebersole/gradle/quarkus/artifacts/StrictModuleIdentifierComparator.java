package com.github.sebersole.gradle.quarkus.artifacts;

import java.util.Comparator;
import java.util.Objects;

/**
 * Strict comparator for ModuleIdentifier references.  Meaning even if the arguments
 * happen to be versioned we ignore that
 */
public class StrictModuleIdentifierComparator implements Comparator<ModuleIdentifier> {
	/**
	 * Singleton access
	 */
	public static final StrictModuleIdentifierComparator INSTANCE = new StrictModuleIdentifierComparator();

	@Override
	public int compare(ModuleIdentifier o1, ModuleIdentifier o2) {
		return Objects.compare( o1.groupArtifact(), o2.groupArtifact(), String::compareTo );
	}
}
