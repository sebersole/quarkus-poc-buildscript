package com.github.sebersole.gradle.quarkus.jandex;

import org.jboss.jandex.Index;

/**
 * Something that can produce a Jandex Index.
 *
 * @apiNote If this creator "wraps" a jar file, the creation could
 * potentially read from a jandex file contained within the jar
 * instead of completely re-indexing.
 */
public interface IndexCreator {
	/**
	 * Create the index
	 */
	Index createIndex();
}
