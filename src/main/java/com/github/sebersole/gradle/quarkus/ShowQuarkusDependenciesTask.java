package com.github.sebersole.gradle.quarkus;

import java.io.File;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.PluginCollection;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.extension.ResolvedExtension;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;

/**
 * @author Steve Ebersole
 */
public class ShowQuarkusDependenciesTask extends DefaultTask {
	public static final String DSL_NAME = "showQuarkusDependencies";

	public ShowQuarkusDependenciesTask() {
		setGroup( QUARKUS );
		setDescription( "Shows dependency information per Quarkus extension.  Can also call `showQuarkusDependencies_<extension>` to limit the info to just the named extension" );
	}

	@TaskAction
	public void showDependencies() {
		final Project project = getProject();
		final QuarkusPlugin quarkusPlugin = project.getPlugins().getPlugin( QuarkusPlugin.class );
		final Services services = quarkusPlugin.getServices();

		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Combined Quarkus dependencies" );
		getLogger().lifecycle( REPORT_BANNER_LINE );
		showConfiguration( services.getBuildDetails().getRuntimeDependencies() );
		showConfiguration( services.getBuildDetails().getDeploymentDependencies() );

		services.getExtensionService().visitResolvedExtension(
				(identifier, resolvedExtension) -> showDependencies( resolvedExtension )
		);
	}

	public void showDependencies(ResolvedExtension extension) {
		getLogger().lifecycle( REPORT_BANNER_LINE );
		getLogger().lifecycle( "Dependencies for the `{}` extension", extension.getExtensionIdentifier().getArtifactName() );
		getLogger().lifecycle( REPORT_BANNER_LINE );

		showConfiguration( extension.getRuntimeDependencies() );
		showConfiguration( extension.getDeploymentDependencies() );
	}

	private void showConfiguration(Configuration dependencies) {
		getLogger().lifecycle("  > {}", dependencies.getName() );

		getLogger().lifecycle( "    > Artifacts" );

		for ( Dependency dependency : dependencies.getAllDependencies() ) {
			final String coordinate = Helper.groupArtifactVersion( dependency.getGroup(), dependency.getName(), dependency.getVersion() );
			getLogger().lifecycle("      > {}", coordinate );
		}

		getLogger().lifecycle( "    > Files" );

		for ( File file : dependencies.resolve() ) {
			getLogger().lifecycle("      > {}", file.getName() );
		}
	}
}
