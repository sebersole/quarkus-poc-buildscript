package com.github.sebersole.gradle.quarkus.jpa;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.jandex.IndexingTask;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS_BUILD_STEPS;

/**
 * @author Steve Ebersole
 */
public class ResolveJpaTask extends DefaultTask {
	public static final String DSL_NAME = "resolveQuarkusJpa";

	private final QuarkusSpec quarkusDsl;
	private final Services services;

	public static ResolveJpaTask apply(QuarkusSpec quarkusDsl, Services services, Project gradleProject) {
		final ResolveJpaTask generateTask = gradleProject.getTasks().create(
				DSL_NAME,
				ResolveJpaTask.class,
				quarkusDsl,
				services,
				gradleProject
		);
		generateTask.setGroup( QUARKUS_BUILD_STEPS );
		generateTask.setDescription( "Resolves JPA persistence-units" );

		final ShowJpaTask showTask = ShowJpaTask.apply( quarkusDsl, services, gradleProject );
		showTask.dependsOn( generateTask );

		generateTask.dependsOn( IndexingTask.DSL_NAME );

		return generateTask;
	}

	@Inject
	public ResolveJpaTask(QuarkusSpec quarkusDsl, Services services, Project gradleProject) {
		this.quarkusDsl = quarkusDsl;
		this.services = services;
	}

	@TaskAction
	public void generateUnits() {
		getLogger().lifecycle( "Starting {} task", DSL_NAME );
		services.findService( JpaService.class ).resolve();
	}
}
