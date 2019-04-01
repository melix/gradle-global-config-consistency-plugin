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
    def "resolves the same versions in all configurations of a single project (#plugin, global=#global)"() {
        given:
        withPlugin(plugin, global)
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
        result.output.contains('By constraint : configuration resolution consistency')

        where:
        plugin | global
        'base' | true
        'base' | false
        'java' | true
        'java' | false
    }

    @Unroll
    def "resolves the same versions in all configurations of distinct projects (#plugin, global=#global)"() {
        given:
        withPlugin(plugin, global)
        file("foo/build.gradle") << """
            dependencies {
                implementation("org.apache.commons:commons-lang3:3.0")
                runtimeOnly("org.apache.commons:commons-lang3:3.3.1")
            }
        """
        file("bar/build.gradle") << """
            dependencies {
                runtimeOnly("org.apache.commons:commons-lang3:3.3.2")
            }
        """

        when:
        def result1 = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(":dependencies", "--configuration", "uberResolve")
                .build()

        then:
        println(result1.output)

        when:
        def result = resolve('foo')
        def expectedVersion = "${global ? '3.3.2' : '3.3.1'}"

        then:
        result.output.contains("org.apache.commons:commons-lang3:3.0 -> $expectedVersion")
        result.output.contains("org.apache.commons:commons-lang3:{strictly $expectedVersion} -> $expectedVersion")
        result.output.contains('By constraint : configuration resolution consistency')

        when:
        result = resolve('bar', 'runtimeClasspath')

        then:
        result.output.contains('org.apache.commons:commons-lang3:{strictly 3.3.2} -> 3.3.2')
        result.output.contains('By constraint : configuration resolution consistency')

        where:
        plugin | global
        'base' | true
        'base' | false
        'java' | true
        'java' | false
    }

    private File withBasePlugin(boolean global=true) {
        if (global) {
            buildFile << """
            plugins {
                id "me.champeau.gradle.config.consistency-base-global"
            }
            """
        } else {
            buildFile << """
            plugins {
                id("me.champeau.gradle.config.consistency-base") apply(false)
            }
            allprojects {
                apply plugin: "me.champeau.gradle.config.consistency-base"
            }
            """
        }
        buildFile << """
            allprojects {
                repositories { jcenter() }
            }
            configurationConsistency {
               usage = 'java-runtime'
            }
            subprojects {
                apply plugin: 'java-library'
            }
        """
        if (!global) {
            buildFile << """
            subprojects {
                configurationConsistency {
                   usage = 'java-runtime'
                }
            }
            """
        }
    }

    private File withJavaPlugin(boolean global=true) {
        if (global) {
            buildFile << """
            plugins {
                id "me.champeau.gradle.config.consistency-java-global"
            }
            """
        } else {
            buildFile << """
            plugins {
                id("me.champeau.gradle.config.consistency-java") apply(false)
            }
            allprojects {
                apply plugin: "me.champeau.gradle.config.consistency-java"
            }
            """
        }
        buildFile << """
            allprojects {
                repositories { jcenter() }
            }
            subprojects {
                apply plugin: 'java-library'
            }
        """
    }

    void withPlugin(String name, boolean global=true) {
        switch (name) {
            case 'java':
                withJavaPlugin(global)
                break
            case 'base':
                withBasePlugin(global)
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

