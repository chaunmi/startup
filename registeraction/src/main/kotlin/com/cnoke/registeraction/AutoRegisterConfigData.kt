package com.cnoke.registeraction

import org.gradle.api.Project
import java.io.Serializable

class AutoRegisterConfigData(var list: ArrayList<RegisterInfoData> = ArrayList<RegisterInfoData>(),
                             var cacheEnabled: Boolean = true,
                             ): Serializable, Cloneable {

//    @Transient
//    var registerInfo: ArrayList<Map<String, Any?>> = ArrayList<Map<String, Any?>>()
//
//    fun convertToRegisterInfo() {
//        registerInfo.forEach { map ->
//            val info = RegisterInfoData()
//            info.interfaceName = map[AutoRegisterConfig.INTERFACE_NAME] as String
//            var superClasses = map["scanSuperClasses"]
//            if (superClasses == null) {
//                superClasses = arrayListOf<String>()
//            } else if (superClasses is String) {
//                val superList =  arrayListOf<String>()
//                superList.add(superClasses)
//                superClasses = superList
//            }
//            info.superClassNames = superClasses as ArrayList<String>
//            info.initClassName = map[AutoRegisterConfig.INSERT_TO_CLASS_NAME] as String //代码注入的类
//            info.initMethodName = map[AutoRegisterConfig.INSERT_TO_METHOD_NAME] as String //代码注入的方法（默认为static块）
//            info.registerMethodName = map[AutoRegisterConfig.REGISTER_METHOD_NAME] as String //生成的代码所调用的方法
//            info.registerClassName = map[AutoRegisterConfig.REGISTER_CLASS_NAME] as String //注册方法所在的类
//            map["include"]?.let {
//                info.include = arrayListOf<String>(*it as Array<String>)
//            }
//            map["exclude"]?.let {
//                info.exclude = arrayListOf<String>(*it as Array<String>)
//            }
//            map[AutoRegisterConfig.CLASS_LIST]?.let {
//                info.classList = arrayListOf<String>(*it as Array<String>)
//            }
//            info.isInstance = map[AutoRegisterConfig.IS_INSTANCE] as Boolean
//            if (info.validate())
//                list.add(info)
//            else {
//                project?.logger?.error("auto register config error: scanInterface, codeInsertToClassName and registerMethodName should not be null\n$info")
//            }
//        }
//    }

    override fun clone(): Any {
        return try {
            super.clone() as AutoRegisterConfigData
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            AutoRegisterConfigData()
        }
    }
}