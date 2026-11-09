plugins {
    id("com.android.library")
}

android {
    namespace = "dev.alexytomi.androidnsbypass"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        prefabPublishing = true
    }

    defaultConfig {
        minSdk = 21 // This is sort of a lie, the lib turns itself off on versions below sdk24
                    // I don't want to figure out android 4 compiling so uh, you get this.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    prefab {
        create("androidnsbypass") {
            headers = "src/main/cpp/include"
        }
    }

    buildTypes {
        getByName("debug") {
            matchingFallbacks += listOf("debug")
        }

        create("proguard") {
            matchingFallbacks += listOf("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
