package com.cnoke.registeraction

import org.gradle.api.Project
import java.io.File
import java.io.Serializable

open class AutoRegisterConfig: Serializable, Cloneable {

    companion object {
        const val INTERFACE_NAME = "scanInterface"
        const val INSERT_TO_CLASS_NAME = "codeInsertToClassName"
        const val INSERT_TO_METHOD_NAME = "codeInsertToMethodName"
        const val REGISTER_CLASS_NAME = "registerClassName"
        const val REGISTER_METHOD_NAME = "registerMethodName"
        const val IS_INSTANCE = "isInstance"
        const val CLASS_LIST = "classList"
    }
    
    var registerInfo = ArrayList<Map<String, Any>>()
    
    var list = ArrayList<RegisterInfo>()

    lateinit var project: Project
    var cacheEnabled = true

    override fun clone(): AutoRegisterConfig {
        return try {
            super.clone() as AutoRegisterConfig
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            AutoRegisterConfig()
        }
    }

    fun convertToRegisterInfo() {
        registerInfo.forEach { map ->
            val info = RegisterInfo()
            info.interfaceName = map[INTERFACE_NAME] as String
            var superClasses = map["scanSuperClasses"]
            if (superClasses == null) {
                superClasses = arrayListOf<String>()
            } else if (superClasses is String) {
                val superList =  arrayListOf<String>()
                superList.add(superClasses)
                superClasses = superList
            }
            info.superClassNames = superClasses as ArrayList<String>
            info.initClassName = map[INSERT_TO_CLASS_NAME] as String //代码注入的类
            info.initMethodName = map[INSERT_TO_METHOD_NAME] as String //代码注入的方法（默认为static块）
            info.registerMethodName = map[REGISTER_METHOD_NAME] as String //生成的代码所调用的方法
            info.registerClassName = map[REGISTER_CLASS_NAME] as String //注册方法所在的类
            map["include"]?.let {
                info.include = arrayListOf<String>(*it as Array<String>)
            }
            map["exclude"]?.let {
                info.exclude = arrayListOf<String>(*it as Array<String>)
            }
            map[CLASS_LIST]?.let {
                info.classList = arrayListOf<String>(*it as Array<String>)
            }
            info.isInstance = map[IS_INSTANCE] as Boolean
            if (info.validate())
                list.add(info)
            else {
                project.logger.error("auto register config error: scanInterface, codeInsertToClassName and registerMethodName should not be null\n$info")
            }
        }
    }

    fun convertConfig() {
        registerInfo.forEach { map ->
            val info = RegisterInfo()
            info.interfaceName = map[INTERFACE_NAME] as String
            var superClasses = map["scanSuperClasses"]
            if (superClasses == null) {
                superClasses = ArrayList<String>()
            } else if (superClasses is String) {
                val superList =  ArrayList<String>()
                superList.add(superClasses)
                superClasses = superList
            }
            info.superClassNames = superClasses as ArrayList<String>
            info.initClassName = map[INSERT_TO_CLASS_NAME] as String //代码注入的类
            info.initMethodName = map[INSERT_TO_METHOD_NAME] as String //代码注入的方法（默认为static块）
            info.registerMethodName = map[REGISTER_METHOD_NAME] as String //生成的代码所调用的方法
            info.registerClassName = map[REGISTER_CLASS_NAME] as String //注册方法所在的类
            info.include = map["include"] as ArrayList<String>
            info.exclude = map["exclude"] as ArrayList<String>
            info.isInstance = map[IS_INSTANCE] as Boolean
            info.init()
            if (info.validate())
                list.add(info)
            else {
                project.logger.error("auto register config error: scanInterface, codeInsertToClassName and registerMethodName should not be null\n$info")
            }

        }

        if (cacheEnabled) {
            checkRegisterInfo()
        } else {
            deleteFile(AutoRegisterHelper.getRegisterInfoCacheFile(project))
            deleteFile(AutoRegisterHelper.getRegisterCacheFile(project))
        }
    }

    private fun checkRegisterInfo() {
        val registerInfo = AutoRegisterHelper.getRegisterInfoCacheFile(project)
        val listInfo = list.toString()
        var sameInfo = false

        if (!registerInfo.exists()) {
            registerInfo.createNewFile()
        } else if(registerInfo.canRead()) {
            val info = AutoRegisterHelper.fileToText(registerInfo)
            sameInfo = info == listInfo
            if (!sameInfo) {
                project.logger.error("startup-register registerInfo has been changed since project(\":${project.name}\") last build")
            }
        } else {
            project.logger.error("startup-register read registerInfo error--------")
        }
        if (!sameInfo) {
            deleteFile(AutoRegisterHelper.getRegisterCacheFile(project))
        }
        if (registerInfo.canWrite()) {
            AutoRegisterHelper.textToFile(listInfo, registerInfo)
        } else {
            project.logger.error("startup-register write registerInfo error--------")
        }
    }

    private fun deleteFile(file: File) {
        if (file.exists()) {
            //registerInfo 配置有改动就删除緩存文件
            file.delete()
        }
    }

    fun reset() {
        list.forEach { info ->
            info.reset()
        }
    }



    override fun toString(): String {
        val sb = StringBuilder(RegisterPlugin.EXT_NAME).append(" = {")
            .append("\n  cacheEnabled = ").append(cacheEnabled)
            .append("\n  registerInfo = [\n")
        val size = list.size
        for(i in 0 until size) {
            sb.append("\t" + list[i].toString().replace("\n", "\n\t"))
            if (i < size - 1)
                sb.append(",\n")
        }
        sb.append("\n  ]\n}")
        return sb.toString()
    }
}