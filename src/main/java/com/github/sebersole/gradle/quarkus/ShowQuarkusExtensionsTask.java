package com.github.sebersole.gradle.quarkus;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusExtensionsTask extends DefaultTask {
	public static final String DSL_NAME = "showQuarkusExtensions";

	@TaskAction
	public void showExtensions() {
		final Project project = getProject();

		final QuarkusPlugin quarkusPlugin = project.getPlugins().getPlugin( QuarkusPlugin.class );


		project.getLogger().lifecycle( "###########################################################" );
		project.getLogger().lifecycle( "Extensions..." );
		project.getLogger().lifecycle( "###########################################################" );

		project.getLogger().lifecycle( "  > Extension artifacts" );
		quarkusPlugin.getAvailableExtensions().forEach(
				(moduleVersionIdentifier, extension) -> {
					project.getLogger().lifecycle( "    > {}", moduleVersionIdentifier.groupArtifactVersion() );
					project.getLogger().lifecycle( "      > {}", extension.getClass().getName() );
				}
		);
	}
}