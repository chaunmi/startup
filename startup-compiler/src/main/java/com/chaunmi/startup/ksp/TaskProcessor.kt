package com.chaunmi.startup.ksp

import com.chaunmi.startup.annotation.StartupInitTask
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

class TaskProcessor : SymbolProcessor, SymbolProcessorProvider {
    private lateinit var logger: KSPLogger
    private lateinit var moduleName: String
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        logger = environment.logger
        moduleName = environment.options["moduleName"]?:""
        return this
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val taskList = resolver.getSymbolsWithAnnotation(StartupInitTask::class.java.name).toList()
        if (taskList.isEmpty()) {
            return emptyList()
        }

        logger.warn("[$moduleName][StartupInitTask] Found InitTasks, size is ${taskList.size}, ${getProcessID()}")

        taskList.forEach {
            checkDeclaration(it)
            val declaration = it as KSDeclaration
            val taskCn = declaration.toClassName()

            logger.warn("[$moduleName][InitTask] Found task: $taskCn")
        //    AutoRegisterHelper.classArray.add(taskCn)
        }
        return emptyList()
    }
    /**
     * 检查注解是否合法
     * 1. 注解类为 Class 类型
     * 2. 注解类实现 IInitTask 接口
     */
    private fun checkDeclaration(annotated: KSAnnotated) {
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
        private const val INIT_TASK_CLASS_NAME = "com.cnoke.startup.task.InitTask"
    }

}