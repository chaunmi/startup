package com.cnoke.registeraction

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class RegisterPlugin : Plugin<Project>{
    override fun apply(project: Project) {
        //返回插件的类型
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        project.extensions.create(EXT_NAME, AutoRegisterConfig::class.java)
        println("RegisterPlugin -->$isApp")
        if(isApp) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                variant.instrumentation.transformClassesWith(CodeInsertTransform::class.java, InstrumentationScope.ALL) {
                    it.registerConfig.set(getRegisterConfigData(project))
                }
                variant.instrumentation.setAsmFramesComputationMode(
                    FramesComputationMode.COPY_FRAMES
                )
            }
        }
    }

    companion object {
       const val EXT_NAME = "autoregister"

        fun getRegisterConfigData(project: Project): AutoRegisterConfigData {
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