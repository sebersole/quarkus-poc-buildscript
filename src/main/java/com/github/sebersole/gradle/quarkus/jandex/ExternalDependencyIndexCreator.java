package com.github.sebersole.gradle.quarkus.jandex;

import java.util.jar.JarFile;

import org.jboss.jandex.Index;

import static com.github.sebersole.gradle.quarkus.jandex.JandexHelper.resolveIndexFromArchive;

/**
 * IndexCreator for external dependencies (jar files)
 */
public class ExternalDependencyIndexCreator implements IndexCreator {
	private final JarFile artifactFile;

	public ExternalDependencyIndexCreator(JarFile artifactFile) {
		this.artifactFile = artifactFile;
	}

	@Override
	public Index createIndex() {
		return resolveIndexFromArchive( artifactFile );
	}
}
