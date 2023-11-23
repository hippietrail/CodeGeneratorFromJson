import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.6.0"
}

android {
    namespace = "com.example.codegeneratorfromjson"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.codegeneratorfromjson"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// All our constants
private val generatedFilePath = "src/main/generated"
private val generatedFileName = "GeneratedFromJson.kt"
private val jsonUrl = "https://my-json-server.typicode.com/typicode/demo/profile"
private val shortTimeout = 45
private val longTimeout = 200
private val generatedVariableName = "jsonFromGeneratedCode"

tasks.register("createGeneratedFile") {
    println("createGeneratedFile")
    doLast {
        val sourceDirectory = File("${project.projectDir}/$generatedFilePath")
        val sourceFile = File(sourceDirectory, generatedFileName)

        val sourceFileAlreadyExisted = sourceFile.isFile

        val timeout = if (sourceFileAlreadyExisted) shortTimeout else longTimeout

        var usableGeneratedSourceFileExists = false

        fetchJson(jsonUrl, timeout)?.let { jsonString ->
            val code = generateCodeFromJson(generatedVariableName, jsonString)
            val ok = sourceFileAlreadyExisted || makeSureFileExists(sourceDirectory, sourceFile)
            if (ok) {
                overwriteFileWithCode(sourceFile, code)
                usableGeneratedSourceFileExists = true
            }
        } ?: run {
            if (sourceFileAlreadyExisted) {
                println("socket timed out but we already have a generated source file from earlier")
                usableGeneratedSourceFileExists = true
            } else {
                val msg = "socket timed out. unable to generate kotlin source file from JSON result."
                println(msg)
                //error(msg) // = IllegalStateException
                throw RuntimeException(msg)
            }
        }

        println("Do we have a usable generated source code file either by writing, overwriting, or from earlier? ${usableGeneratedSourceFileExists}")

        if (usableGeneratedSourceFileExists) {
            addGeneratedFilePath(generatedFilePath)
        }
    }
}

tasks.named("preBuild") {
    dependsOn("createGeneratedFile")
}

tasks.register("cleanGeneratedFile") {
    doLast {
        val generatedDir = File("${project.projectDir}/$generatedFilePath")

        if (generatedDir.exists()) {
            val generatedFile = File("${project.projectDir}/$generatedFilePath/$generatedFileName")
            if (generatedFile.exists()) {
                generatedFile.delete()
                println("Deleted the generated file: $generatedFile")
            } else {
                println("The generated file does not exist: $generatedFile")
            }

            val dsStore = File("${project.projectDir}/$generatedFilePath/.DS_Store")
            if (dsStore.exists()) {
                dsStore.delete()
                println("Deleted .DS_Store")
            } else {
                println(".DS_Store does not exist")
            }

            generatedDir.delete()
            println("Deleted the generated Directory: $generatedDir")
        } else {
            println("The generated directory does not exist: $generatedDir")
        }
    }
}

tasks.named("clean") {
    dependsOn("cleanGeneratedFile")
}

fun fetchJson(jsonUrl: String, timeout: Int): String? {
    val conn: HttpURLConnection = URL(jsonUrl).openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.connectTimeout = timeout

    try {
        println("fetching JSON with timeout of ${conn.connectTimeout}")
        conn.connect()
        return conn.inputStream.bufferedReader().use { it.readText() }
    } catch (e: SocketTimeoutException) {
        return null
    }
}

fun generateCodeFromJson(generatedVariableName: String, jsonString: String): String {
    val code = StringBuilder()
    code.append("package ${android.defaultConfig.applicationId}\n\n")
    code.append("internal val $generatedVariableName = \"\"\"\n$jsonString\"\"\"\n")
    return code.toString()
}

fun makeSureFileExists(sourceDirectory: File, sourceFile: File): Boolean {
    if (sourceFile.isFile)
        return true

    // returns true if dirs were made but we can proceed if it didn't make them because they already existed
    if (!sourceDirectory.isDirectory) {
        try {
            sourceDirectory.mkdirs()
        } catch (e: Exception) {
            // don't return fail yet. maybe the directory was already there
        }
    }

    if (sourceDirectory.isDirectory) {
        return try {
            return sourceFile.createNewFile()
        } catch (e: Exception) {
            false
        }
    }
    return false
}

fun overwriteFileWithCode(sourceFile: File, code: String) {
    println("(over)writing Kotlin source code into the file")
    FileWriter(sourceFile).use { writer ->
        writer.write(code)
    }
}

fun addGeneratedFilePath(generatedFilePath: String) {
    // Add the generated code to the project build logic
    println("adding the generated file path")
    kotlin {
        sourceSets {
            main {
                kotlin {
                    kotlin.srcDir(generatedFilePath)
                }
            }
        }
    }
}
