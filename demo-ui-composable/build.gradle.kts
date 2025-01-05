import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

version = "2.0.0"

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
//    js {
//        browser()
//        nodejs()
//    }

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
                implementation(project(":bigtext-ui-composable"))

                // for demo hash calculation on text change
                implementation("org.kotlincrypto.hash:sha2:0.5.3")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.sunnychung.lib.multiplatform.bigtext.demo.MainKt"
        jvmArgs += listOf("-XX:MaxMetaspaceSize=100m", "-Xmx1280m", "-XX:MinHeapFreeRatio=10", "-XX:MaxHeapFreeRatio=20", "-XX:InitiatingHeapOccupancyPercent=75", "-XX:+HeapDumpOnOutOfMemoryError")
        jvmArgs += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" // to enable debugger for debug use only
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xmx1280m")

    testLogging {
        events = setOf(TestLogEvent.STARTED, TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
