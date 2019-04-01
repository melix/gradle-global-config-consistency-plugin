package me.champeau.gradle.configcst

import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Project

class BaseLocalConfigConsistencyPlugin: AbstractConfigConsistencyPlugin() {
    override
    fun getProjectsToInclude(thisProject: Project): Set<Project> = setOf(thisProject)

    override
    fun validateUsage(project: Project) = project.pluginManager.withPlugin("me.champeau.gradle.global.config.consistency-base") {
        throw InvalidUserCodeException("The global configuration consistency plugin should only be applied on the root project")
    }
}