package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import com.github.sebersole.gradle.quarkus.extension.ExtensionService;
import com.github.sebersole.gradle.quarkus.extension.ResolvedExtension;

/**
 * @author Steve Ebersole
 */
public class QuarkusPlugin implements Plugin<Project> {
	public static final String META_INF = "META-INF/";
	public static final String DEPLOYMENT_ARTIFACT_KEY = "deployment-artifact";


	private Services services;
	private QuarkusSpec dsl;

	public Services getServices() {
		return services;
	}

	public QuarkusSpec getDsl() {
		return dsl;
	}

	@Override
	public void apply(Project project) {
		dsl = project.getExtensions().create( "quarkus", QuarkusSpec.class, project );
		services = new Services( dsl, project );

		final ShowQuarkusExtensionsTask showExtensionsTask = project.getTasks().create( ShowQuarkusExtensionsTask.DSL_NAME, ShowQuarkusExtensionsTask.class );
		final ShowQuarkusDependenciesTask showDependenciesTask = project.getTasks().create( ShowQuarkusDependenciesTask.DSL_NAME, ShowQuarkusDependenciesTask.class );

		project.getTasks().addRule(
				"Pattern: showQuarkusDependencies_<extension>",
				taskName -> {
					if ( taskName.startsWith( "showQuarkusDependencies_" )
							&& ! taskName.endsWith( "showQuarkusDependencies_" ) ) {
						// parse the extension name
						final int delimiterPosition = taskName.indexOf( '_' );
						assert delimiterPosition > 1;
						final String extensionName = taskName.substring( delimiterPosition + 1 );

						final Task ruleTask = project.getTasks().create( taskName );
						ruleTask.doLast(
								(task1) -> {
									final ExtensionService extensionService = services.getExtensionService();
									final ResolvedExtension extension = extensionService.findRegisteredExtensionByName( extensionName );

									showDependenciesTask.showDependencies( extension );
								}
						);
					}
				}
		);

		project.afterEvaluate( p -> services.prepareForConfiguration() );

		project.getGradle().getTaskGraph().whenReady( graph -> services.prepareForProcessing() );
	}
}
