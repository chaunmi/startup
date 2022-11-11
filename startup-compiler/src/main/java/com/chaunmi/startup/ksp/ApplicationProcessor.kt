package com.chaunmi.startup.ksp

import com.chaunmi.startup.annotation.StartupInitApplication
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

class ApplicationProcessor : SymbolProcessor, SymbolProcessorProvider {
    private lateinit var logger: KSPLogger
    private lateinit var moduleName: String
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        logger = environment.logger
        moduleName = environment.options["moduleName"]?:""
        return this
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val applicationList = resolver.getSymbolsWithAnnotation(StartupInitApplication::class.java.name).toList()
        if (applicationList.isEmpty()) {
            return emptyList()
        }

        logger.warn("[$moduleName][StartupInitApplication] Found InitApplications, size is ${applicationList.size}, ${getProcessID()}")

        applicationList.forEach {
            checkDeclaration(it)
            val declaration = it as KSDeclaration
            val taskCn = declaration.toClassName()

            logger.warn("[$moduleName][InitApplication] Found application: $taskCn")
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
            "Type [${annotated}] with annotation [${StartupInitApplication::class.java.name}] should be a class"
        }
        checkNotNull(annotated.getAllSuperTypes().find {
            it.declaration.toClassName() == INIT_APPLICATION_CLASS_NAME
        }) {
            "Type [${annotated.toClassName()}] with annotation [${StartupInitApplication::class.java.name}] should extends [$INIT_APPLICATION_CLASS_NAME]"
        }
    }

    companion object {
        private const val INIT_APPLICATION_CLASS_NAME = "com.cnoke.startup.application.IApplication"
    }
}