package com.github.sebersole.gradle.quarkus.jpa;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.service.Services;

import static com.github.sebersole.gradle.quarkus.Helper.QUARKUS;
import static com.github.sebersole.gradle.quarkus.Helper.REPORT_BANNER_LINE;

/**
 * @author Steve Ebersole
 */
public class ShowJpaTask extends DefaultTask {
	public static final String DSL_NAME = "showQuarkusJpa";

	private final QuarkusSpec quarkusDsl;
	private final Services services;
	private final Project gradleProject;

	public static ShowJpaTask apply(QuarkusSpec quarkusDsl, Services services, Project gradleProject) {
		final ShowJpaTask task = gradleProject.getTasks().create( DSL_NAME, ShowJpaTask.class, quarkusDsl, services, gradleProject );
		task.setGroup( QUARKUS );
		task.setDescription( "Displays Quarkus JPA persistence-units" );
		return task;
	}

	@Inject
	public ShowJpaTask(QuarkusSpec quarkusDsl, Services services, Project gradleProject) {
		this.quarkusDsl = quarkusDsl;
		this.services = services;
		this.gradleProject = gradleProject;
	}

	@TaskAction
	public void showPersistenceUnits() {
		final JpaService jpaService = services.findService( JpaService.class );

		final Logger logger = getProject().getLogger();

		logger.lifecycle( REPORT_BANNER_LINE );
		logger.lifecycle( "Quarkus JPA persistence-units" );
		logger.lifecycle( REPORT_BANNER_LINE );

		jpaService.forEach(
				persistenceUnit -> {
					logger.lifecycle( "  > {}", persistenceUnit.getUnitName() );
					logger.lifecycle( "    > Managed classes", persistenceUnit.getUnitName() );
					if ( persistenceUnit.getClassesToInclude().isEmpty() ) {
						logger.lifecycle( "      > (none)" );
					}
					else {
						persistenceUnit.getClassesToInclude().forEach(
								classInfo -> logger.lifecycle( "      > {}", classInfo.name().toString() )
						);
					}
				}
		);
	}
}
