package me.champeau.gradle.configcst

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import java.util.function.Predicate

open class ConfigurationConsistencyExtension(project: Project) {
    val usage: Property<String> = project.objects.property()

    var includeConfiguration: Predicate<Configuration> = Predicate {
        it.isBucket
    }

    private
    val Configuration.isBucket
        get() = !(isCanBeConsumed || isCanBeResolved)

}