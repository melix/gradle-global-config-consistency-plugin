package me.champeau.gradle.configcst

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage

class JavaLocalConfigConsistencyPlugin: Plugin<Project> {
    override
    fun apply(project: Project) = project.run {
        pluginManager.apply(BaseLocalConfigConsistencyPlugin::class.java)
        extensions.getByType(ConfigurationConsistencyExtension::class.java).usage.set(Usage.JAVA_RUNTIME)
    }
}