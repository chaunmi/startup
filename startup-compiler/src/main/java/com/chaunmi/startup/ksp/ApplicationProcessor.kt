package com.chaunmi.startup.ksp

import com.chaunmi.startup.annotation.StartupInitApplication
import com.chaunmi.startup.annotation.StartupInitTask
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.text.SimpleDateFormat

class ApplicationProcessor : SymbolProcessor, SymbolProcessorProvider {
    private lateinit var logger: KSPLogger
    private lateinit var moduleName: String
    private lateinit var codeGenerator: CodeGenerator
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        logger = environment.logger
        moduleName = environment.options["moduleName"]?:""
        codeGenerator = environment.codeGenerator
        return this
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val applicationList = resolver.getSymbolsWithAnnotation(StartupInitApplication::class.java.name).toList()
        val taskList = resolver.getSymbolsWithAnnotation(StartupInitTask::class.java.name).toList()
        if (applicationList.isEmpty() && taskList.isEmpty()) {
            return emptyList()
        }

        val applicationJsonArray = JsonArray()
        applicationList.forEach {
            checkApplicationDeclaration(it)
            val declaration = it as KSDeclaration
            val applicationClassName = declaration.toClassName()
            applicationJsonArray.add(applicationClassName)
            logger.warn("[$moduleName][InitApplication] Found application: $applicationClassName,  ${declaration.qualifiedName?.asString()}")
        }
        val taskJsonArray = JsonArray()
        taskList.forEach {
            checkTaskDeclaration(it)
            val declaration = it as KSDeclaration
            val taskClassName = declaration.toClassName()
            taskJsonArray.add(taskClassName)
            logger.warn("[$moduleName][InitTask] Found task: $taskClassName,  ${declaration.qualifiedName?.asString()}")
        }

        val jsonObject = JsonObject()
        jsonObject.add("applicationList", applicationJsonArray)
        jsonObject.add("taskList", taskJsonArray)
        val file =
            codeGenerator.createNewFile(Dependencies.ALL_FILES, "startup", "${moduleName}_startup_config", "json")

        file.write(jsonObject.toString().toByteArray())

        return emptyList()
    }

    /**
     * 检查注解是否合法
     * 1. 注解类为 Class 类型
     * 2. 注解类实现 IInitTask 接口
     */
    private fun checkApplicationDeclaration(annotated: KSAnnotated) {
        check(annotated is KSClassDeclaration) {
            "Type [${annotated}] with annotation [${StartupInitApplication::class.java.name}] should be a class"
        }
        checkNotNull(annotated.getAllSuperTypes().find {
            it.declaration.toClassName() == INIT_APPLICATION_CLASS_NAME
        }) {
            "Type [${annotated.toClassName()}] with annotation [${StartupInitApplication::class.java.name}] should extends [$INIT_APPLICATION_CLASS_NAME]"
        }
    }

    private fun checkTaskDeclaration(annotated: KSAnnotated) {
        check(annotated is KSClassDeclaration) {
            "Type [${annotated}] with annotation [${StartupInitTask::class.java.name}] should be a class"
        }
        checkNotNull(annotated.getAllSuperTypes().find {
            it.declaration.toClassName() == INIT_TASK_CLASS_NAME
        }) {
            "Type [${annotated.toClassName()}] with annotation [${StartupInitTask::class.java.name}] should extends [$INIT_TASK_CLASS_NAME]"
        }
    }

    companion object {
        private const val INIT_APPLICATION_CLASS_NAME = "com.cnoke.startup.application.IApplication"
        private const val INIT_TASK_CLASS_NAME = "com.cnoke.startup.task.InitTask"
    }
}