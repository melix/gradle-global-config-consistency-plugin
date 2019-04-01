plugins {
    `kotlin-dsl`
    groovy
    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation(gradleTestKit())
    testImplementation("org.codehaus.groovy:groovy:2.5.6")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5") {
        exclude(group = "org.codehaus.groovy")
    }
}

group = "me.champeau.gradle.configcst"
version = "1.0"

gradlePlugin {
    plugins {
        register("globalConfigurationConsistencyPlugin") {
            id = "me.champeau.gradle.config.consistency-base-global"
            implementationClass = "me.champeau.gradle.configcst.BaseGlobalConfigConsistencyPlugin"
        }
        register("glogalConfigurationConsistencyPluginJava") {
            id = "me.champeau.gradle.config.consistency-java-global"
            implementationClass = "me.champeau.gradle.configcst.JavaGlobalConfigConsistencyPlugin"
        }
        register("localConfigurationConsistencyPlugin") {
            id = "me.champeau.gradle.config.consistency-base"
            implementationClass = "me.champeau.gradle.configcst.BaseLocalConfigConsistencyPlugin"
        }
        register("localConfigurationConsistencyPluginJava") {
            id = "me.champeau.gradle.config.consistency-java"
            implementationClass = "me.champeau.gradle.configcst.JavaLocalConfigConsistencyPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("${buildDir}/repo")
        }
    }
}

