package com.chaunmi.startup.kapt

import com.chaunmi.startup.annotation.StartupInitApplication
import com.chaunmi.startup.annotation.StartupInitTask
import com.google.auto.service.AutoService
import java.text.SimpleDateFormat
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class ApplicationProcessor: AbstractProcessor() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private lateinit var moduleName: String
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        Log.setLogger(processingEnv.messager)
        moduleName = processingEnv.options["moduleName"]?:""
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val supportAnnotationTypes = mutableSetOf<String>()
        supportAnnotationTypes.add(StartupInitTask::class.java.canonicalName)
        supportAnnotationTypes.add(StartupInitApplication::class.java.canonicalName)
        return supportAnnotationTypes
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        println("[kapt][${dateFormat.format(System.currentTimeMillis())}][$moduleName] ApplicationProcessor process  ")
    //    Log.i("[kapt] ApplicationProcessor process  ")
        val taskElements = roundEnv?.getElementsAnnotatedWith(StartupInitTask::class.java)
        val applicationElements = roundEnv?.getElementsAnnotatedWith(StartupInitApplication::class.java)
        if ((taskElements == null || taskElements.isEmpty()) &&
            (applicationElements == null || applicationElements.isEmpty())) {
            return false
        }

        println("[kapt][StartupInitTask] Found tasks, size is ${taskElements?.size}")
        println("[kapt][StartupInitApplication] Found applications, size is ${applicationElements?.size}")
    //    Log.i("[InitTask] Found tasks, size is ${taskElements?.size}")
    //    Log.i("[InitApplication] Found applications, size is ${taskElements?.size}")

        return true
    }
}