package com.github.sebersole.gradle.quarkus;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

import com.github.sebersole.gradle.quarkus.extensions.StandardExtensionSpec;
import groovy.lang.Closure;

public class QuarkusSpec {
    private final Project project;
    private final ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> extensionSpecContainer;

    public QuarkusSpec(Project project) {
        this.project = project;
        this.extensionSpecContainer = generateExtensionSpecContainer( project );
    }

    private static ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> generateExtensionSpecContainer(Project project) {
        final ObjectFactory objectFactory = project.getObjects();

        final ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> extensionSpecContainer = objectFactory.polymorphicDomainObjectContainer( ExtensionSpec.class );

        // NOTE: the major weakness here at the moment is that for any extensions that need additional config options
        //      (e.g. the orm `databaseFamily` and `persistenceUnits {}`) we need to have knowledge of that at plugin
        //      build time.
        //
        //      done a few things to help with that:
        //          1) moved resolution of extension artifacts into the Gradle `buildscript {}` phase.  this makes the classes
        //              contained in the jar/project available to the script.  This very usefully gives us up-front knowledge
        //              of the extensions that will be used, including the deployment artifact
        //          2) moving forward we could leverage this to use information from those deployment artifacts to help drive
        //              this processing - e.g. allow it to provide a specialized `ExtensionSpec` DSL block.  `HibernateOrmExtensionSpec`
        //              is an example of what that might look like.  "discovering" it is mocked in
        //              `AvailableExtension#from` to see what its effect might be

        // this does not work - research how this is supposed to work
        //extensionSpecContainer.registerBinding( ExtensionSpec.class, StandardExtensionSpec.class );

        extensionSpecContainer.registerFactory(
                ExtensionSpec.class,
                (name) -> {
                    project.getLogger().debug( "Creating (default) `StandardExtensionSpec({})`", name );
                    //return new StandardExtensionSpec ( name, objectFactory );
                    // the benefit of doing the following, compared to above, is that Gradle will automatically inject the returned
                    // object with a bunch of extra goodies
                    return objectFactory.newInstance( StandardExtensionSpec.class, name );
                }
        );

        extensionSpecContainer.registerFactory(
                StandardExtensionSpec.class,
                (name) -> {
                    project.getLogger().debug( "Creating `StandardExtensionSpec({})`", name );
                    return objectFactory.newInstance( StandardExtensionSpec.class, name );
                }
        );

        extensionSpecContainer.registerFactory(
                SpecialExtensionSpec.class,
                name -> {
                    project.getLogger().debug( "Creating `SpecialExtensionSpec({})`", name );
                    return objectFactory.newInstance( SpecialExtensionSpec.class, name );
                }
        );

        return extensionSpecContainer;
    }

    public void extensionSpecs(Action<PolymorphicDomainObjectContainer<ExtensionSpec>> action) {
        project.getLogger().lifecycle( "Access to `extensionSpecs` container via Action" );
        action.execute( extensionSpecContainer );
    }

    public void extensionSpecs(Closure<PolymorphicDomainObjectContainer<ExtensionSpec>> closure) {
        project.getLogger().lifecycle( "Access to `extensionSpecs` container via Closure" );
        ConfigureUtil.configure( closure, extensionSpecContainer );
    }

    public ExtensiblePolymorphicDomainObjectContainer<ExtensionSpec> getExtensionSpecContainer() {
        return extensionSpecContainer;
    }
}