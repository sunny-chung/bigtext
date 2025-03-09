plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("sunnychung.publication")
}

version = "2.1.0"

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
                api(project(":bigtext-datastructure"))
                implementation(kotlin("reflect"))
                implementation("com.lodborg:interval-tree:1.0.0") // Interval Tree
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
