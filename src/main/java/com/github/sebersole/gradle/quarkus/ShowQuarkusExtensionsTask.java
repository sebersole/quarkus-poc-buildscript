package com.github.sebersole.gradle.quarkus;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.extension.ExtensionService;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusExtensionsTask extends DefaultTask {
	public static final String DSL_NAME = "showQuarkusExtensions";

	@TaskAction
	public void showExtensions() {
		final Project project = getProject();
		final QuarkusPlugin quarkusPlugin = project.getPlugins().getPlugin( QuarkusPlugin.class );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Available Extensions

		final ExtensionService extensionService = quarkusPlugin.getServices().getExtensionService();

		project.getLogger().lifecycle( Helper.REPORT_BANNER_LINE );
		project.getLogger().lifecycle( "Available Extensions" );
		project.getLogger().lifecycle( Helper.REPORT_BANNER_LINE );

		extensionService.getAvailableExtensions().forEach(
				(moduleIdentifier, extension) -> {
					project.getLogger().lifecycle( "  > {}", moduleIdentifier.groupArtifactVersion() );
				}
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// DSL extensions

		final ExtensionContainer gradleExtensionsContainer = quarkusPlugin.getDsl().getExtensions();

		project.getLogger().lifecycle( Helper.REPORT_BANNER_LINE );
		project.getLogger().lifecycle( "Registered DSL Extensions" );
		project.getLogger().lifecycle( Helper.REPORT_BANNER_LINE );

		gradleExtensionsContainer.getExtensionsSchema().getElements().forEach(
				extensionSchema -> {
					if ( DslExtensionSpec.class.isAssignableFrom( extensionSchema.getPublicType().getConcreteClass() ) ) {
						final String dslExtensionName = extensionSchema.getName();
						final DslExtensionSpec spec = (DslExtensionSpec) gradleExtensionsContainer.getByName( dslExtensionName );
						project.getLogger().lifecycle( "  > {} - {}", dslExtensionName, spec.getDisplayInfo() );
					}
				}
		);
	}
}
