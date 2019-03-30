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
        register("configurationConsistencyPlugin") {
            id = "me.champeau.gradle.global.config.consistency-base"
            implementationClass = "me.champeau.gradle.configcst.BaseGlobalConfigConsistencyPlugin"
        }
        register("configurationConsistencyPluginJava") {
            id = "me.champeau.gradle.global.config.consistency-java"
            implementationClass = "me.champeau.gradle.configcst.JavaGlobalConfigConsistencyPlugin"
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

