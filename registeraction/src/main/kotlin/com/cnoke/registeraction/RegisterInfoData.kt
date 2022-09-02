package com.cnoke.registeraction

import java.io.Serializable

class RegisterInfoData(    //以下是可配置参数
    var interfaceName: String = "",
    var superClassNames: ArrayList<String> = arrayListOf<String>(),
    var initClassName: String = "",
    var initMethodName: String = "",
    var registerClassName: String = "",
    var registerMethodName: String = "",
    var include: ArrayList<String> = arrayListOf<String>(),
    var exclude: ArrayList<String> = arrayListOf<String>(),
    var isInstance: Boolean = false,
    var classList: ArrayList<String> = arrayListOf<String>()
): Serializable, Cloneable {

    fun validate(): Boolean {
        return interfaceName.isNotEmpty() && registerClassName.isNotEmpty() && registerMethodName.isNotEmpty()
    }

    override fun clone(): RegisterInfoData {
        return try {
            super.clone() as RegisterInfoData
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            RegisterInfoData()
        }
    }

    companion object {
        /**
         * 将.转换为斜杠/
         * @param str String
         * @return String
         */
        fun convertDotToSlash(str: String?): String {
            return if(!str.isNullOrEmpty()) str.replace(".", "/").intern() else ""
        }

        fun convertSlashToDot(str: String?): String {
            return if(!str.isNullOrEmpty()) str.replace("/", ".").intern() else ""
        }
    }
}