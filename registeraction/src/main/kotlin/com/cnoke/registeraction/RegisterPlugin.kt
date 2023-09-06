package com.cnoke.registeraction

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import java.io.File

class RegisterPlugin : Plugin<Project>{
    override fun apply(project: Project) {
        //返回插件的类型
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        project.extensions.create(EXT_NAME, AutoRegisterConfig::class.java)
        project.logger.log(LogLevel.INFO, " RegisterPlugin -->$isApp ")
        println("RegisterPlugin -->$isApp")
        if(isApp) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

            println("name: ${project.name}, subprojects size: ${project.subprojects.size}")
            project.parent?.allprojects?.forEach {
                println("subproject name: ${it.name}")
            }
            androidComponents.onVariants { variant ->
                if(variant.name == "debug") {
                    AutoRegisterHelper.classArray.forEach {
                        println(" find class $it ")
                    }

                    project.logger.log(LogLevel.INFO, "==================== codeInsert start ${variant.name}=======================")
                    variant.instrumentation.transformClassesWith(CodeInsertTransform::class.java, InstrumentationScope.ALL) {
                        it.registerConfig.set(getRegisterConfigData(project, variant = variant.name))
                    }
                    variant.instrumentation.setAsmFramesComputationMode(
                        FramesComputationMode.COPY_FRAMES
                    )
                    project.logger.log(LogLevel.INFO, "==================== codeInsert end ${variant.name}=======================")
                }
            }

//            androidComponents.onVariants { variant ->
//                if(variant.name == "debug") {
//                    println("==================== codeInsert scan start ${variant.name}=======================")
//                    variant.instrumentation.transformClassesWith(CodeScanTransform::class.java, InstrumentationScope.ALL) {
//                        it.registerConfig.set(getRegisterConfigData(project))
//                    }
//                    variant.instrumentation.setAsmFramesComputationMode(
//                        FramesComputationMode.COPY_FRAMES
//                    )
//                    println("==================== codeInsert scan end ${variant.name}=======================")
//                }
//            }
        }
    }

    companion object {
       const val EXT_NAME = "autoregister"

        fun getRegisterConfigData(project: Project, variant: String): AutoRegisterConfigData {
//            var config = project.extensions.findByName(EXT_NAME) as? AutoRegisterConfigData
//
//            if(config == null || config.registerInfo.isEmpty()){
//                config =  AutoRegisterConfigData()
//            }
            val config =  AutoRegisterConfigData()
//            val startup:HashMap<String, Any> = hashMapOf()
//            startup[AutoRegisterConfig.INTERFACE_NAME] = "com.cnoke.startup.application.IApplication"
//            startup[AutoRegisterConfig.INSERT_TO_CLASS_NAME] = "com.cnoke.startup.FinalAppRegister"
//            startup[AutoRegisterConfig.INSERT_TO_METHOD_NAME] = "init"
//            startup[AutoRegisterConfig.REGISTER_CLASS_NAME] = "com.cnoke.startup.FinalAppRegister"
//            startup[AutoRegisterConfig.REGISTER_METHOD_NAME] = "register"
//            startup[AutoRegisterConfig.IS_INSTANCE] = true
//            startup[AutoRegisterConfig.CLASS_LIST] = arrayOf("com/cnoke/test1/Test1", "com/cnoke/test2/Test2")
//            config.registerInfo.add(startup)
//
//            val initTask: HashMap<String, Any> = hashMapOf()
//            initTask[AutoRegisterConfig.INTERFACE_NAME] = "com.cnoke.startup.task.InitTask"
//            initTask[AutoRegisterConfig.INSERT_TO_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
//            initTask[AutoRegisterConfig.INSERT_TO_METHOD_NAME] = "init"
//            initTask[AutoRegisterConfig.REGISTER_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
//            initTask[AutoRegisterConfig.REGISTER_METHOD_NAME] = "register"
//            initTask[AutoRegisterConfig.IS_INSTANCE] = false
//            initTask[AutoRegisterConfig.CLASS_LIST] = arrayOf("com/cnoke/test1/Task1", "com/cnoke/test1/Task11",
//                "com/cnoke/test1/Task12", "com/cnoke/test1/Task13", "com/cnoke/test2/Task2",
//                "com/cnoke/test2/Task21", "com/cnoke/test2/Task22")
//            config.registerInfo.add(initTask)

            val initApp = RegisterInfoData()
            initApp.isInstance = true
            initApp.classList = arrayListOf("com.cnoke.test1.Test1", "com.cnoke.test2.Test2")
            initApp.initClassName = "com.cnoke.startup.FinalAppRegister"
            initApp.interfaceName = "com.cnoke.startup.application.IApplication"
            initApp.initMethodName = "init"
            initApp.registerClassName = "com.cnoke.startup.FinalAppRegister"
            initApp.registerMethodName = "register"

            config.list.add(initApp)

            val initTask = RegisterInfoData()
            initTask.isInstance = false
            initTask.classList = arrayListOf("com.cnoke.test1.Task1", "com.cnoke.test1.Task11",
                "com.cnoke.test1.Task12", "com.cnoke.test1.Task13", "com.cnoke.test2.Task2",
                "com.cnoke.test2.Task21", "com.cnoke.test2.Task22")
            initTask.initClassName = "com.cnoke.startup.FinalTaskRegister"
            initTask.interfaceName = "com.cnoke.startup.task.InitTask"
            initTask.initMethodName = "init"
            initTask.registerClassName = "com.cnoke.startup.FinalTaskRegister"
            initTask.registerMethodName = "register"


            config.list.add(initTask)

            project.parent?.allprojects?.forEach {
                println("subproject name: ${it.name}")
                config.moduleList.add(AutoRegisterHelper.getKspFileDir(it) + variant + File.separator + "resources" + File.separator + "startup" + File.separator + "${it.name}_startup_config.json")
            }
        //    config.project = project
        //    config.convertToRegisterInfo()
            return config
        }

        fun getRegisterConfig(project: Project): AutoRegisterConfig {
            var config = project.extensions.findByName(EXT_NAME) as? AutoRegisterConfig

            if(config == null || config.registerInfo.isEmpty()){
                config =  AutoRegisterConfig()
            }

            val startup:HashMap<String, Any> = hashMapOf()
            startup[AutoRegisterConfig.INTERFACE_NAME] = "com.cnoke.startup.application.IApplication"
            startup[AutoRegisterConfig.INSERT_TO_CLASS_NAME] = "com.cnoke.startup.FinalAppRegister"
            startup[AutoRegisterConfig.INSERT_TO_METHOD_NAME] = "init"
            startup[AutoRegisterConfig.REGISTER_CLASS_NAME] = "com.cnoke.startup.FinalAppRegister"
            startup[AutoRegisterConfig.REGISTER_METHOD_NAME] = "register"
            startup[AutoRegisterConfig.IS_INSTANCE] = true
            startup[AutoRegisterConfig.CLASS_LIST] = arrayOf("com/cnoke/test1/Test1", "com/cnoke/test2/Test2")
            config.registerInfo.add(startup)

            val initTask: HashMap<String, Any> = hashMapOf()
            initTask[AutoRegisterConfig.INTERFACE_NAME] = "com.cnoke.startup.task.InitTask"
            initTask[AutoRegisterConfig.INSERT_TO_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
            initTask[AutoRegisterConfig.INSERT_TO_METHOD_NAME] = "init"
            initTask[AutoRegisterConfig.REGISTER_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
            initTask[AutoRegisterConfig.REGISTER_METHOD_NAME] = "register"
            initTask[AutoRegisterConfig.IS_INSTANCE] = false
            initTask[AutoRegisterConfig.CLASS_LIST] = arrayOf("com/cnoke/test1/Task1", "com/cnoke/test1/Task11",
                "com/cnoke/test1/Task12", "com/cnoke/test1/Task13", "com/cnoke/test2/Task2",
                "com/cnoke/test2/Task21", "com/cnoke/test2/Task22")
            config.registerInfo.add(initTask)

            config.project = project
            config.convertToRegisterInfo()
            return config
        }
    }

}