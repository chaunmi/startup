package com.cnoke.registeraction

import java.io.File
import java.io.Serializable
import java.util.regex.Pattern

class RegisterInfo: Serializable, Cloneable {
    //以下是可配置参数
    var interfaceName = ""
    var superClassNames = arrayListOf<String>()
    var initClassName = ""
    var initMethodName = ""
    var registerClassName = ""
    var registerMethodName = ""
    var include = arrayListOf<String>()
    var exclude = arrayListOf<String>()
    var isInstance = false

    //以下不是可配置参数
    @Transient
    var includePatterns = arrayListOf<Pattern>()
    @Transient
    var excludePatterns =  arrayListOf<Pattern>()

    var classList = arrayListOf<String>()
    @Transient
    var fileContainsInitClass: File? = null //initClassName的class文件或含有initClassName类的jar文件
    fun reset() {
        fileContainsInitClass = null
        classList.clear()
    }

    fun validate(): Boolean {
        return interfaceName.isNotEmpty() && registerClassName.isNotEmpty() && registerMethodName.isNotEmpty()
    }

    override fun clone(): RegisterInfo {
        return try {
            super.clone() as RegisterInfo
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            RegisterInfo()
        }
    }

    override fun toString(): String {
        val sb = StringBuilder("{")
        sb.append("\n\t").append("scanInterface").append("\t\t\t=\t").append(interfaceName)
        sb.append("\n\t").append("scanSuperClasses").append("\t\t=\t[")
        for(i in 0 until superClassNames.size) {
            if(i > 0) sb.append(",")
            sb.append(" \"").append(superClassNames[i]).append("\"")
        }
        sb.append(" ]")
        sb.append("\n\t").append("codeInsertToClassName").append("\t=\t").append(initClassName)
        sb.append("\n\t").append("codeInsertToMethodName").append("\t=\t").append(initMethodName)
        sb.append("\n\t").append("registerMethodName").append("\t\t=\tpublic static void ")
            .append(registerClassName).append(".").append(registerMethodName)
        sb.append("\n\t").append("include").append(" = [")
        include.forEach { i ->
            sb.append("\n\t\t\"").append(i).append("\"")
        }
        sb.append("\n\t]")
        sb.append("\n\t").append("exclude").append(" = [")
        exclude.forEach { i ->
            sb.append("\n\t\t\"").append(i).append("\"")
        }
        sb.append("\n\t]\n}")
        return sb.toString()
    }

    fun init() {
        if (include.isEmpty()) include.add(".*") //如果没有设置则默认为include所有
        if (registerClassName.isEmpty())
            registerClassName = initClassName

        //将interfaceName中的'.'转换为'/'
        if (interfaceName.isNotEmpty())
            interfaceName = convertDotToSlash(interfaceName)
        //将superClassName中的'.'转换为'/'
        for(i in 0 until superClassNames.size) {
            val superClass = convertDotToSlash(superClassNames[i])
            superClassNames[i] = superClass
        }
        //注册和初始化的方法所在的类默认为同一个类
        initClassName = convertDotToSlash(initClassName)
        //默认插入到static块中
        if (initMethodName.isEmpty())
            initMethodName = "<clinit>"
        registerClassName = convertDotToSlash(registerClassName)
        //添加默认的排除项
        DEFAULT_EXCLUDE.forEach { e ->
            if (!exclude.contains(e))
                exclude.add(e)
        }
        initPattern(include, includePatterns)
        initPattern(exclude, excludePatterns)
    }

    companion object {
        val DEFAULT_EXCLUDE = arrayOf(
            ".*/R(\\$[^/]*)?",
            ".*/BuildConfig$"
        )

        /**
         * 将.转换为斜杠/
         * @param str String
         * @return String
         */
        fun convertDotToSlash(str: String): String {
            return if(str.isNotEmpty()) str.replace("\\.", "/").intern() else str
        }

        fun initPattern(list: ArrayList<String>, patterns: ArrayList<Pattern>) {
            list.forEach {
                patterns.add(Pattern.compile(it))
            }
        }

    }
}