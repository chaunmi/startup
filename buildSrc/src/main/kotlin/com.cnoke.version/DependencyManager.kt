
/**
 * @date on 2021/12/28
 * @author huanghui
 * @title
 * @describe
 */

object Versions {
    val appcompat = "1.2.0"
    val coreKtx = "1.3.2"
    val constraintlayout = "2.0.4"
    val kotlin = "1.4.20"
    val coroutine = "1.3.9"
}

object AndroidX {
    val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    val material = "com.google.android.material:material:1.1.0"
}

object Kt {
    val stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val stdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val coroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutine}"
    val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutine}"
}

object Depend{
    val startupApi = "io.github.cnoke.startup:api:1.1.5-LOCAL"
    val startupAnnotation = "io.github.cnoke.startup:annotation:1.0.0-LOCAL"

    val kspApiVersion = "1.6.10-1.0.4"
    val kspApi = "com.google.devtools.ksp:symbol-processing-api:${kspApiVersion}"

    val kspCompilerVersion = "1.0.2-LOCAL"
    val kspStartupCompiler = "io.github.cnoke.startup:ksp-compiler:$kspCompilerVersion"
}

object ASM {
    val asm_version = "9.2"
    val asm =  "org.ow2.asm:asm:$asm_version"
    val asmUtil =  "org.ow2.asm:asm-util:$asm_version"
    val asmCommons = "org.ow2.asm:asm-commons:$asm_version"
    val asmTree = "org.ow2.asm:asm-tree:$asm_version"
}