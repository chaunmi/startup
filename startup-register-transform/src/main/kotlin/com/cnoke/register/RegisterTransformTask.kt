package com.cnoke.register

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.component.external.model.ComponentVariant
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

abstract class RegisterTransformTask: DefaultTask() {
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Input
    abstract var registerConfig: AutoRegisterConfig

    @TaskAction
    fun transform() {
        val startTime = System.currentTimeMillis()
        println(" transform start ")
        registerConfig.reset()

        var cacheEnabled = registerConfig.cacheEnabled

        var jarManagerFile: File? = null
        var cacheMap: HashMap<String, ScanJarHarvest>? = null
        var cacheFile: File? = null
        var gson: Gson? = null

        if(cacheEnabled) {
            gson = Gson()
            cacheFile = AutoRegisterHelper.getRegisterCacheFile(project)
            if(cacheFile.exists()) {
                cacheFile.delete()
            }
            val typeToken = object : TypeToken<HashMap<String, ScanJarHarvest>>() {

            }
            cacheMap = AutoRegisterHelper.readToMap(cacheFile, typeToken.type)
        }

        val scanProcessor = CodeScanProcessor(registerConfig.list, cacheMap)

        JarOutputStream(
            output.get().asFile
                .outputStream()
                .buffered()
        ).use { output ->

            var scanStartTime = System.currentTimeMillis()
            allJars.get().forEach { jar ->
                if(!scanProcessor.scanJarNew(jar.asFile)) {
                    println(" outputToJar  name: ${jar.asFile.absolutePath}")
                    outputToJar(jar.asFile, output)
                }
            }

            println(" scan jars end cost: " + (System.currentTimeMillis() - scanStartTime))

            scanStartTime = System.currentTimeMillis()

            allDirectories.get().forEach { dir ->
                scanClasses(output, dir.asFile, scanProcessor)
            }

            println(" scan classes end cost: " + (System.currentTimeMillis() - scanStartTime))

            val fileContainsInitClassList = arrayListOf<String>()

            val tempFilePath = AutoRegisterHelper.getCacheFileDir(project)

            FileUtils.cleanDirectory(File(tempFilePath))

            println(" clean startup-register cache $tempFilePath ")

            registerConfig.list.forEach { registerInfo ->
                println(" execute insert $registerInfo ")
                if(registerInfo.fileContainsInitClassPath.isNotEmpty()) {
                    val filePath = registerInfo.fileContainsInitClassPath

                    val srcFile = File(filePath)
                    val destFilePath = tempFilePath + srcFile.name
                    val destFile = File(destFilePath)

                    if(!destFile.exists()) {
                        println(" copy $filePath to $destFilePath ")
                        FileUtils.copyFile(srcFile, destFile)
                    }
                    registerInfo.fileContainsInitClassPath = destFilePath

                    println("insert register code to file path: " + registerInfo.fileContainsInitClassPath)
                    if(registerInfo.classList.isEmpty()) {
                        project.logger.error("No class implements found for interface:" + registerInfo.interfaceName)
                    }else {
                        registerInfo.classList.forEach {
                            println(it)
                        }
                        CodeInsertProcessor.insertInitCodeTo(registerInfo)

                        val count = RegisterInfo.getFileContainsInitClassCount(filePath)
                        // 不存在同一个jar包中的多个文件需要执行插入代码操作，直接打包到输出jar中
                        if(count == 1) {
                            outputToJar(File(registerInfo.fileContainsInitClassPath), output,
                                registerInfo.fileContainsInitClassEntryName)
                        }else if(count > 1){
                            if(!fileContainsInitClassList.contains(registerInfo.fileContainsInitClassPath)) {
                                fileContainsInitClassList.add(registerInfo.fileContainsInitClassPath)
                            }
                        }
                    }
                }else {
                    project.logger.error("The specified register class not found:" + registerInfo.registerClassName)
                }
            }

            fileContainsInitClassList.forEach {
                println(" outputToJar $it ")
                outputToJar(File(it), output)
            }
            //最后输出存在多次插入同一个jar包的文件
//            RegisterInfo.fileContainsInitClassMap.forEach { (filePath, count) ->
//                if(count > 1) {
//                    println(" outputToJar $filePath ")
//                    outputToJar(File(filePath), output)
//                }
//            }
        }
        println(" transform end, cost:  " + (System.currentTimeMillis() - startTime))

    }

    private fun outputToJar(inputFile: File, jarOutput: JarOutputStream, inputEntryName: String? = null) {
        if(inputFile.name.endsWith(".jar")) {
            val jarFile = JarFile(inputFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                kotlin.runCatching {
                    jarOutput.putNextEntry(JarEntry(jarEntry.name))
                    jarFile.getInputStream(jarEntry).use {
                        it.copyTo(jarOutput)
                    }
                }
                jarOutput.closeEntry()
            }
            jarFile.close()
        }else {
            if(!inputEntryName.isNullOrEmpty()) {
                println(" adding from directory $inputEntryName")
                jarOutput.putNextEntry(JarEntry(inputEntryName))
                inputFile.inputStream().use { inputStream ->
                    inputStream.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
        }
    }

    private fun scanClasses(
        output: JarOutputStream,
        rootDir: File,
        scanProcessor: CodeScanProcessor
    ) {
        rootDir.walk().forEach { child ->
            if (child.isDirectory) return@forEach
            if (!child.name.endsWith(".class")) return@forEach
            val name = child.toRelativeString(rootDir)
            val entryName = name.replace(File.separatorChar, '/')
            println(" scanClasses name: $name, path: ${rootDir.absolutePath}, entryName: $entryName")

            if(!scanProcessor.checkInitClassNew(entryName, child.absolutePath)) {
                outputToJar(child, output, entryName)
            }else {
                println(" scanClasses find name: $entryName, path: ${rootDir.absolutePath}")
            }
            if(scanProcessor.shouldProcessClass(entryName)) {
                scanProcessor.scanClass(child)
            }
        }
    }

//    private fun transform(
//        name: String,
//        input: InputStream,
//        output: JarOutputStream,
//    ) = runCatching {
//        val entry = ZipEntry(name)
//        output.putNextEntry(entry)
//
//        val cr = ClassReader(input)
//        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
//        cr.accept(TraceClassVisitor(Opcodes.ASM9, cw), ClassReader.EXPAND_FRAMES)
//
//        output.write(cw.toByteArray())
//        output.closeEntry()
//    }

}