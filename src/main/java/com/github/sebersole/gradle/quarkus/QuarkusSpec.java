package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

public abstract class QuarkusSpec implements ExtensionAware {
    private final Project project;

    public QuarkusSpec(Project project) {
        this.project = project;
    }
}