package com.cnoke.register

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.util.Locale

class RegisterPlugin : Plugin<Project>{

    override fun apply(project: Project) {
        //返回插件的类型
        // com.android.application                      -> AppPlugin::class.java
        // com.android.library                          -> LibraryPlugin::class.java
        // com.android.test                             -> TestPlugin::class.java
        // com.android.dynamic-feature, added in 3.2    -> DynamicFeaturePlugin::class.java
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        project.extensions.create(EXT_NAME, AutoRegisterConfig::class.java)
        project.logger.log(LogLevel.INFO, " RegisterPlugin -->$isApp ")
        println("RegisterPlugin -->$isApp")
        if(isApp) {
            project.plugins.withType(AppPlugin::class.java) {
                val androidComponents =
                    project.extensions.findByType(AndroidComponentsExtension::class.java)
                androidComponents?.onVariants { variant ->
                    val capitalizeName = variant.name.replaceFirstChar {
                        if (it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else
                            it.toString()
                    }
                    val name = "transform${capitalizeName}Startup"
                    println("taskName: $name")

                    val taskProvider = project.tasks.register(name, RegisterTransformTask::class.java)
                    taskProvider.get().registerConfig = getRegisterConfig(project)

                    variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                        .use(taskProvider)
                        .toTransform(
                            ScopedArtifact.CLASSES,
                            RegisterTransformTask::allJars,
                            RegisterTransformTask::allDirectories,
                            RegisterTransformTask::output,
                        )
                }
            }
        }
    }

    companion object {
       const val EXT_NAME = "autoregister"

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
            config.registerInfo.add(startup)

            val initTask: HashMap<String, Any> = hashMapOf()
            initTask[AutoRegisterConfig.INTERFACE_NAME] = "com.cnoke.startup.task.InitTask"
            initTask[AutoRegisterConfig.INSERT_TO_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
            initTask[AutoRegisterConfig.INSERT_TO_METHOD_NAME] = "init"
            initTask[AutoRegisterConfig.REGISTER_CLASS_NAME] = "com.cnoke.startup.FinalTaskRegister"
            initTask[AutoRegisterConfig.REGISTER_METHOD_NAME] = "register"
            initTask[AutoRegisterConfig.IS_INSTANCE] = false
            config.registerInfo.add(initTask)

            config.project = project
            config.convertConfig()
            return config
        }
    }

}