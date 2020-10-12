package com.github.sebersole.gradle.quarkus.jandex;

import org.jboss.jandex.Index;

/**
 * Something that can produce a Jandex Index
 */
public interface IndexCreator {
	/**
	 * Singleton access
	 */
	IndexCreator NO_OP_CREATOR = () -> null;

	Index createIndex();
}
