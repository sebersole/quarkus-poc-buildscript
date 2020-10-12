package com.github.sebersole.gradle.quarkus;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.jupiter.api.Test;

import com.github.sebersole.gradle.quarkus.orm.HibernateOrmExtension;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class SimpleTest {

	@Test
	public void testShowExtensions() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner(
				"simple",
				ShowQuarkusExtensionsTask.DSL_NAME
		);

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":" + ShowQuarkusExtensionsTask.DSL_NAME );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );

		// because hibernate-orm config specified derby for the family
		assertThat( buildResult.getOutput(), containsString( "quarkus-jdbc-derby" ) );
	}

	@Test
	public void testShowDependencies() {
		final GradleRunner gradleRunner = TestHelper.createGradleRunner(
				"simple",
				ShowQuarkusDependenciesTask.DSL_NAME
		);

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":" + ShowQuarkusDependenciesTask.DSL_NAME );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );
	}

	@Test
	public void testShowLimitedDependencies() {
		final String taskName = ShowQuarkusDependenciesTask.DSL_NAME + "_" + HibernateOrmExtension.DSL_NAME;

		final GradleRunner gradleRunner = TestHelper.createGradleRunner(
				"simple",
				taskName
		);

		final BuildResult buildResult = gradleRunner.build();

		final BuildTask taskResult = buildResult.task( ":" + taskName );
		assertThat( taskResult, notNullValue() );
		assertThat( taskResult.getOutcome(), is( TaskOutcome.SUCCESS ) );
	}

}
