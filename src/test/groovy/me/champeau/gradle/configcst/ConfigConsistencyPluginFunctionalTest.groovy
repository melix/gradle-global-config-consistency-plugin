package me.champeau.gradle.configcst

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class ConfigConsistencyPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = file('settings.gradle')
        buildFile = file('build.gradle')
        settingsFile << """
            rootProject.name = 'multi'
            include "foo"
            include "bar"
        """
    }

    @Unroll
    def "resolves the same versions in all configurations of a single project (#plugin)"() {
        given:
        WithPlugin(plugin)
        file("foo/build.gradle") << """
            dependencies {
                implementation("org.apache.commons:commons-lang3:3.0")
                runtimeOnly("org.apache.commons:commons-lang3:3.3.1")
            }
        """

        when:
        def result = resolve()

        then:
        result.output.contains('org.apache.commons:commons-lang3:3.0 -> 3.3.1')
        result.output.contains('By constraint : Global configuration consistency')

        where:
        plugin << [ 'base', 'java']
    }

    @Unroll
    def "resolves the same versions in all configurations of distinct projects (#plugin)"() {
        given:
        WithPlugin(plugin)
        file("foo/build.gradle") << """
            dependencies {
                implementation("org.apache.commons:commons-lang3:3.0")
            }
        """
        file("bar/build.gradle") << """
            dependencies {
                runtimeOnly("org.apache.commons:commons-lang3:3.3.1")
            }
        """

        when:
        def result = resolve('foo')

        then:
        result.output.contains('org.apache.commons:commons-lang3:3.0 -> 3.3.1')
        result.output.contains('By constraint : Global configuration consistency')

        when:
        result = resolve('bar', 'runtimeClasspath')

        then:
        result.output.contains('org.apache.commons:commons-lang3:{strictly 3.3.1} -> 3.3.1')
        result.output.contains('By constraint : Global configuration consistency')

        where:
        plugin << [ 'base', 'java']
    }

    private File withBasePlugin() {
        buildFile << """
            plugins {
                id "me.champeau.gradle.global.config.consistency-base"
            }
            allprojects {
                repositories { jcenter() }
            }
            globalConfigurationConsistency {
               usage = 'java-runtime'
            }
            subprojects {
                apply plugin: 'java-library'
            }
        """
    }

    private File withJavaPlugin() {
        buildFile << """
            plugins {
                id "me.champeau.gradle.global.config.consistency-java"
            }
            allprojects {
                repositories { jcenter() }
            }
            subprojects {
                apply plugin: 'java-library'
            }
        """
    }

    void WithPlugin(String name) {
        switch (name) {
            case 'java':
                withJavaPlugin()
                break
            case 'base':
                withBasePlugin()
                break
        }
    }

    File file(String path) {
        def file = new File(testProjectDir.root, path)
        file.parentFile.mkdirs()
        file
    }

    BuildResult resolve(String project = 'foo', String config = 'compileClasspath', String dep = 'commons-lang3') {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("${project}:dependencyInsight", '--configuration', config, '--dependency', dep, "-S")
                .build()
    }
}

