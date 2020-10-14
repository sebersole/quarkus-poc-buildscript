package com.github.sebersole.gradle.quarkus.jandex;

import org.gradle.api.tasks.SourceSet;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

/**
 * IndexCreator for a local project
 */
public class ProjectIndexCreator implements IndexCreator {
	private final SourceSet mainSourceSet;

	private Index resolvedIndex;
	private boolean resolved;

	public ProjectIndexCreator(SourceSet mainSourceSet) {
		this.mainSourceSet = mainSourceSet;
	}

	@Override
	public Index createIndex() {
		final Indexer indexer = new Indexer();
		mainSourceSet.getOutput().getClassesDirs().forEach(
				file -> JandexHelper.applyDirectory( file, indexer )
		);
		return indexer.complete();
	}
}
