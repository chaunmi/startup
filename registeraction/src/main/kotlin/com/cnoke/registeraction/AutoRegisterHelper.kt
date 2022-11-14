package com.cnoke.registeraction

import com.android.SdkConstants
import com.google.gson.Gson
import org.gradle.api.Project
import java.io.File
import java.io.FileNotFoundException
import java.lang.management.ManagementFactory
import java.lang.reflect.Type

object AutoRegisterHelper {
    const val CACHE_INFO_DIR = "startup-register"
    const val KSP_DIR = "ksp"

    val classArray = ArrayList<String>()

    fun getProcessID(): String {
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        return runtimeMXBean.name + "@" + Integer.valueOf(runtimeMXBean.name.split("@")[0]).toInt()
    }
    /**
     * 缓存自动注册配置的文件
     * @param project
     * @return file
     */
    fun  getRegisterInfoCacheFile(project: Project): File {
        val baseDir = getCacheFileDir(project)
        if (mkdirs(baseDir)) {
            return File(baseDir + "register-info.config")
        } else {
            throw FileNotFoundException("Not found  path:$baseDir")
        }
    }

    /**
     * 缓存扫描到结果的文件
     * @param project
     * @return File
     */
    fun  getRegisterCacheFile(project: Project): File {
        val baseDir = getCacheFileDir(project)
        if (mkdirs(baseDir)) {
            return  File(baseDir + "register-cache.json")
        } else {
            throw  FileNotFoundException("Not found  path:$baseDir")
        }
    }
    /**
     * 将扫描到的结果缓存起来
     * @param cacheFile
     * @param harvests
     */
    fun cacheRegisterHarvest(cacheFile: File? , harvests: String?) {
        if (cacheFile == null || harvests.isNullOrEmpty())
            return
        cacheFile.parentFile.mkdirs()
        if (!cacheFile.exists())
            cacheFile.createNewFile()
        textToFile(harvests, cacheFile)
    }

    private fun  getCacheFileDir(project: Project): String {
        return project.buildDir.absolutePath + File.separator + SdkConstants.FD_INTERMEDIATES + File.separator + CACHE_INFO_DIR + File.separator
    }

    fun  getKspFileDir(project: Project): String {
        return project.buildDir.absolutePath + File.separator + SdkConstants.FD_GENERATED + File.separator + KSP_DIR + File.separator
    }


    /**
     * 读取文件内容并创建Map
     * @param file 缓存文件
     * @param type map的类型
     * @return
     */
    fun readToMap(file: File, type: Type): Map<String, ScanJarHarvest> {
        var map: Map<String, ScanJarHarvest>? = null
        if (file.exists()) {
            if (type != null) {
                val text = fileToText(file)
                        if (text.isNotEmpty()) {
                            try {
                                map =  Gson().fromJson(text, type)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
            }
        }
        if (map == null) {
            map = HashMap()
        }
        return map
    }

    fun fileToText(file: File): String {
        if(!file.exists()) {
            return ""
        }
        return file.bufferedReader().use { it.readText() }
    }

    fun textToFile(str: String?, file: File?) {
        if(str.isNullOrEmpty() || file == null || !file.exists()) {
            return
        }
        file.bufferedWriter().use { it.write(str) }
    }
    /**
     * 创建文件夹
     * @param dirPath
     * @return boolean
     */
    fun  mkdirs(dirPath: String): Boolean {
        val baseDirFile = File(dirPath)
        var isSuccess = true
        if (!baseDirFile.isDirectory) {
            isSuccess = baseDirFile.mkdirs()
        }
        return isSuccess
    }

}