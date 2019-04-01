package me.champeau.gradle.configcst

import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Project

class BaseGlobalConfigConsistencyPlugin : AbstractConfigConsistencyPlugin() {
    override
    fun getProjectsToInclude(thisProject: Project): Set<Project> = thisProject.allprojects

    override
    fun validateUsage(project: Project) {
        if (project != project.rootProject) {
            throw InvalidUserCodeException("The global configuration consistency plugin should only be applied on the root project")
        }
    }

}
