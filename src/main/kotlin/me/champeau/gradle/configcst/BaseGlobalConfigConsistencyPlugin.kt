package me.champeau.gradle.configcst

import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Usage

class BaseGlobalConfigConsistencyPlugin : Plugin<Project> {

    override
    fun apply(project: Project) = project.run {
        validateUsage()
        val extension = createExtension()
        val uberResolveConf = createUberResolveConfiguration(extension)
        subprojects {
            uberResolveConf.dependencies.add(
                    dependencies.create(this)
            )
        }
        allprojects {
            configureResolution(uberResolveConf, project)
        }
    }

    private
    fun Project.createExtension() = extensions.create("globalConfigurationConsistency", ConfigurationConsistencyExtension::class.java, this)

    private
    fun Project.validateUsage() {
        if (this != rootProject) {
            throw InvalidUserCodeException("The global configuration consistency plugin should only be applied on the root project")
        }
    }

    private
    fun Project.configureResolution(uberResolve: Configuration, target: Project) {
        configurations.all {
            val cnf = this
            if (cnf != uberResolve && isCanBeResolved) {
                incoming.beforeResolve {
                    uberResolve.incoming.resolutionResult.allDependencies {
                        if (this is ResolvedDependencyResult && requested is ModuleComponentSelector) {
                            val s = selected.moduleVersion!!
                            cnf.dependencyConstraints.add(
                                    target.dependencies.constraints.create("${s.group}:${s.name}") {
                                        version {
                                            strictly(s.version)
                                        }
                                        because("Global configuration consistency")
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    private
    fun Project.createUberResolveConfiguration(extension: ConfigurationConsistencyExtension): Configuration = configurations.create("uberResolve") {
        isCanBeConsumed = false
        isCanBeResolved = true
        val cnf = this
        project.afterEvaluate {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, extension.usage.get()))
            }
            configurations.all {
                if (extension.includeConfiguration.test(this)) {
                    cnf.extendsFrom(this)
                }
            }
        }
    }

}
