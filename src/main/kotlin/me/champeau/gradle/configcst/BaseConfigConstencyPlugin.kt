package me.champeau.gradle.configcst

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage

abstract class AbstractConfigConsistencyPlugin : Plugin<Project> {
    companion object {
        val UBER_ELEMENTS_ATTRIBUTE = Attribute.of("me.champeau.config.consistency", Boolean::class.javaObjectType)
    }

    override
    fun apply(project: Project) = project.run {
        validateUsage(project)
        val extension = createExtension()
        val uberResolveConf = createUberResolveConfiguration(extension)
        getProjectsToInclude(this).forEach {
            createUberElementsConfiguration(it, extension)
            configureProjectResolution(it, uberResolveConf)
            if (project != it) {
                // cross-project dependencies
                uberResolveConf.dependencies.add(
                        dependencies.create(it)
                )
            } else {
                // local project dependencies
                project.afterEvaluate {
                    configurations.all {
                        if (extension.includeConfiguration.test(this)) {
                            uberResolveConf.extendsFrom(this)
                        }
                    }
                }
            }
        }
    }

    private
    fun Project.createExtension() = extensions.create("configurationConsistency", ConfigurationConsistencyExtension::class.java, this)

    abstract
    fun validateUsage(project: Project)

    abstract
    fun getProjectsToInclude(thisProject: Project): Set<Project>

    private
    fun createUberElementsConfiguration(target: Project, extension: ConfigurationConsistencyExtension) = target.run {
        val uberElements = configurations.create("uberElements") {
            isCanBeConsumed = true
            isCanBeResolved = false
            attributes {
                attribute(UBER_ELEMENTS_ATTRIBUTE, true)
            }
        }
        afterEvaluate {
            uberElements.attributes {
                attribute(Usage.USAGE_ATTRIBUTE, target.objects.named(Usage::class.java, extension.usage.get()))
            }
            configurations.all {
                if (extension.includeConfiguration.test(this)) {
                    uberElements.extendsFrom(this)
                }
            }
        }
    }

    private
    fun configureProjectResolution(target: Project, uberResolve: Configuration) {
        target.configurations.all {
            val cnf = this
            incoming.beforeResolve {
                if (cnf != uberResolve) {
                    uberResolve.incoming.resolutionResult.allDependencies {
                        if (this is ResolvedDependencyResult && requested is ModuleComponentSelector) {
                            val s = selected.moduleVersion!!
                            cnf.dependencyConstraints.add(
                                    target.dependencies.constraints.create("${s.group}:${s.name}") {
                                        version {
                                            strictly(s.version)
                                        }
                                        because("configuration resolution consistency")
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
        project.afterEvaluate {
            attributes {
                attribute(UBER_ELEMENTS_ATTRIBUTE, true)
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, extension.usage.get()))
            }
        }
    }

}