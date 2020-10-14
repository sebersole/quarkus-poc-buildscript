package com.github.sebersole.gradle.quarkus.datasource;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.sebersole.gradle.quarkus.Helper;
import com.github.sebersole.gradle.quarkus.QuarkusSpec;
import com.github.sebersole.gradle.quarkus.extension.ExtensionContributionState;
import com.github.sebersole.gradle.quarkus.service.Services;

/**
 * @author Steve Ebersole
 */
public class ResolveDataSourcesTask extends DefaultTask {
	public static final String DSL_NAME = "resolveQuarkusDataSources";
	private final QuarkusSpec quarkusDsl;
	private final Services services;

	public static ResolveDataSourcesTask apply(ExtensionContributionState contributionState) {
		final ResolveDataSourcesTask task = contributionState.getGradleProject().getTasks().create(
				DSL_NAME,
				ResolveDataSourcesTask.class,
				contributionState.getQuarkusDsl(),
				contributionState.getServices()
		);
		task.setGroup( Helper.QUARKUS_BUILD_STEPS );
		task.setDescription( "Resolves Quarkus defined data-sources" );
		return task;
	}

	@Inject
	public ResolveDataSourcesTask(QuarkusSpec quarkusDsl, Services services) {
		this.quarkusDsl = quarkusDsl;
		this.services = services;
	}

	@TaskAction
	public void resolveDataSources() {
		services.findService( DataSourceService.class ).resolve();
	}
}
