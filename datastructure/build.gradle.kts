import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
    id("sunnychung.publication")
}

version = "2.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
//    js {
//        browser()
//        nodejs()
//    }
//
//    val appleTargets = listOf(
//        iosArm64(),
//        iosSimulatorArm64(),
//        iosX64(),
//        watchosArm64(),
//        watchosSimulatorArm64(),
//        watchosX64(),
//        tvosArm64(),
//        tvosSimulatorArm64(),
//        tvosX64(),
//        macosArm64(),
//        macosX64()
//    )

    sourceSets {
        commonMain {
            dependencies {
                implementation("co.touchlab:kermit:1.0.0")
                implementation("io.github.sunny-chung:kdatetime-multiplatform:1.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xmx6144m")

    testLogging {
        events = setOf(TestLogEvent.STARTED, TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
