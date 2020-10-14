package com.github.sebersole.gradle.quarkus.jandex;

import java.io.File;

import org.gradle.api.file.RegularFile;

import org.jboss.jandex.Index;

import com.github.sebersole.gradle.quarkus.service.Services;
import com.github.sebersole.gradle.quarkus.artifacts.ModuleVersionIdentifier;

/**
 * @author Steve Ebersole
 */
public class IndexManager {
	private final ModuleVersionIdentifier dependencyId;

	private final RegularFile indexFile;
	private final IndexCreator indexCreator;
	private final Services services;

	private boolean resolved;
	private Index index;

	public IndexManager(
			ModuleVersionIdentifier dependencyId,
			RegularFile indexFile,
			IndexCreator indexCreator,
			Services services) {
		this.dependencyId = dependencyId;
		this.indexFile = indexFile;
		this.indexCreator = indexCreator;
		this.services = services;
	}

	public ModuleVersionIdentifier getDependencyIdentifier() {
		return dependencyId;
	}

	public ModuleVersionIdentifier getModuleVersionIdentifier() {
		return getDependencyIdentifier();
	}

	public RegularFile getIndexFile() {
		return indexFile;
	}

	public Index getIndex() {
		if ( ! resolved ) {
			throw new IllegalStateException( "Index not yet resolved : " + getDependencyIdentifier().groupArtifactVersion() );
		}
		return index;
	}

	public boolean isResolved() {
		return resolved;
	}

	public Index generateIndex() {
		if ( resolved ) {
			throw new IllegalStateException( "Already resolved" );
		}

		index = indexCreator.createIndex();
		services.getIndexingService().getCompositeIndex().expand( index );
		JandexHelper.writeIndexToFile( indexFile.getAsFile(), index );
		resolved = true;

		return index;
	}

	public Index readIndex() {
		if ( resolved ) {
			throw new IllegalStateException( "Already resolved" );
		}

		final File indexFileFile = indexFile.getAsFile();

		if ( ! indexFileFile.exists() ) {
			throw new IllegalStateException( "Attempt to resolve-by-read from non-existent index file : " + indexFileFile.getAbsolutePath() );
		}

		index = JandexHelper.readJandexIndex( indexFileFile );
		services.getIndexingService().getCompositeIndex().expand( index );
		resolved = true;

		return index;
	}
}
