package com.cnoke.register

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

class CodeScanProcessor(val infoList: ArrayList<RegisterInfo>,
                            val cacheMap: HashMap<String, ScanJarHarvest>?) {

    private val cachedJarContainsInitClass = HashSet<String>()

    fun scanJarNew(jarFile: File): Boolean {
        var srcFilePath = jarFile.absolutePath
        var file = JarFile(jarFile)
        var enumeration = file.entries()
        var foundInitClass = false
        val startTime = System.currentTimeMillis()
        println(" scanJar start $srcFilePath ")
        while (enumeration.hasMoreElements()) {
            var jarEntry = enumeration.nextElement() as JarEntry
            var entryName = jarEntry.name
            //support包不扫描
            if (entryName.startsWith("android/support") || entryName.startsWith("androidx"))
                break

            var found = checkInitClassNew(entryName, srcFilePath)
            if(found) {
                println(" scanJar find jarFilePath: $srcFilePath, entryName: $entryName")
                foundInitClass = found
            }
            //是否要过滤这个类，这个可配置
            if (shouldProcessClass(entryName)) {
                val inputStream = file.getInputStream(jarEntry)
                scanClass(inputStream, jarFile.absolutePath)
                inputStream.close()
            }
        }
        file.close()
        println(" scanJar end cost: ${System.currentTimeMillis() - startTime} ,  $srcFilePath ")
        return foundInitClass
    }

    fun checkInitClassNew(entryName: String, fileContainsInitClassPath: String): Boolean {
        if (!entryName.endsWith(".class"))
            return false
        //去掉后缀class
        val newEntryName = entryName.substring(0, entryName.lastIndexOf('.'))
        println(" checkInitClassNew entryName: $entryName , path: $fileContainsInitClassPath ")
        var found = false
        infoList.forEach { ext ->
            if (ext.initClassName == newEntryName) {
                ext.fileContainsInitClassPath = fileContainsInitClassPath
                RegisterInfo.addFileContainsInitClassToMap(fileContainsInitClassPath)
                if (fileContainsInitClassPath.endsWith(".jar")) {
                    addToCacheMap(null, newEntryName, fileContainsInitClassPath)
                }
                if(fileContainsInitClassPath.endsWith(".class")) {
                    ext.fileContainsInitClassEntryName = entryName
                }
                found = true
            }
        }
        return found
    }

    // file in folder like these
    //com/billy/testplugin/Aop.class
    //com/billy/testplugin/BuildConfig.class
    //com/billy/testplugin/R$attr.class
    //com/billy/testplugin/R.class
    // entry in jar like these
    //android/support/v4/BuildConfig.class
    //com/lib/xiwei/common/util/UiTools.class
    fun shouldProcessClass(entryName: String): Boolean {
        if (!entryName.endsWith(".class"))
            return false

        val newEntryName = entryName.substring(0, entryName.lastIndexOf('.'))
        val length = infoList.size
        for(i in 0 until length) {
            if (shouldProcessThisClassForRegister(infoList[i], newEntryName))
                return true
        }
        return false
    }


    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    fun scanClass(file: File): Boolean {
        return scanClass(BufferedInputStream(FileInputStream(file)), file.absolutePath)
    }

    //refer hack class when object init
    fun scanClass(inputStream: InputStream, filePath: String ): Boolean {
        val cr =  ClassReader(inputStream)
        val cw =  ClassWriter(cr, 0)
        val cv =  ScanClassVisitor(Opcodes.ASM6, cw, filePath)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()

        return cv.isFound()
    }

    inner class ScanClassVisitor(api: Int, cv: ClassVisitor, private val filePath: String) : ClassVisitor(api, cv) {
        private var found = false

        fun isAnd(access: Int, flag: Int): Boolean {
            return (access and flag) == flag
        }

        fun isFound(): Boolean {
            return found
        }

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, name, signature, superName, interfaces)
            //抽象类、接口、非public等类无法调用其无参构造方法
            if (isAnd(access, Opcodes.ACC_ABSTRACT)
            || isAnd(access, Opcodes.ACC_INTERFACE)
            || !isAnd(access, Opcodes.ACC_PUBLIC)
            ) {
                return
            }
            infoList.forEach { ext ->
                if (shouldProcessThisClassForRegister(ext, name)) {
                    if (superName != "java/lang/Object" && ext.superClassNames.isNotEmpty()) {
                        for(i in 0 until ext.superClassNames.size) {
                            if (ext.superClassNames[i] == superName) {
                                //  println("superClassNames--------"+name)
                                ext.classList.add(name) //需要把对象注入到管理类 就是fileContainsInitClass
                                found = true
                                addToCacheMap(superName, name, filePath)
                                return
                            }
                        }
                    }
                    if (!ext.interfaceName.isNullOrEmpty() && interfaces != null) {
                        interfaces.forEach { itName ->
                            if (itName == ext.interfaceName) {
                                ext.classList.add(name)//需要把对象注入到管理类  就是fileContainsInitClass
                                addToCacheMap(itName, name, filePath)
                                found = true
                            }
                        }
                    }
                }
            }
        }

    }
    /**
     * 扫描到的类添加到map
     * @param interfaceName
     * @param name
     * @param srcFilePath
     */
    private fun addToCacheMap(interfaceName: String?, name: String?, srcFilePath: String) {
        if (!srcFilePath.endsWith(".jar") || cacheMap == null) return
        var jarHarvest = cacheMap[srcFilePath]
        if (jarHarvest == null) {
            jarHarvest = ScanJarHarvest()
            cacheMap[srcFilePath] = jarHarvest
        }
        if (!name.isNullOrEmpty()) {
            val classInfo = ScanJarHarvest.Harvest()
            classInfo.isInitClass = interfaceName.isNullOrEmpty()
            classInfo.interfaceName = interfaceName
            classInfo.className = name
            jarHarvest.harvestList.add(classInfo)
        }
    }

    fun isCachedJarContainsInitClass(filePath: String): Boolean {
        return cachedJarContainsInitClass.contains(filePath)
    }

    /**
     * 检查是否存在缓存，有就添加class list 和 设置fileContainsInitClass
     * @param jarFile
     * @param destFile
     * @return 是否存在缓存
     */
    fun hitCache(jarFile: File, destFile: File): Boolean {
        if (cacheMap != null) {
            val jarFilePath = jarFile.absolutePath
            val scanJarHarvest = cacheMap[jarFilePath]
            if (scanJarHarvest != null) {
                infoList.forEach { info ->
                    scanJarHarvest.harvestList.forEach { harvest ->
                        //       println("----harvest-------"+harvest.className)
                        if (harvest.isInitClass) {
                            if (info.initClassName == harvest.className) {
                                info.fileContainsInitClassPath = destFile.absolutePath
                                cachedJarContainsInitClass.add(jarFilePath)
                            }
                        } else if (info.interfaceName == harvest.interfaceName) {
                            info.classList.add(harvest.className)
                        }

                        for(i in 0 until info.superClassNames.size) {
                            if (info.superClassNames[i] == harvest.interfaceName) {
                                info.classList.add(harvest.className)
                            }
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * 过滤器进行过滤
         * @param info
         * @param entryName
         * @return
         */
        private  fun shouldProcessThisClassForRegister(info: RegisterInfo, entryName: String): Boolean {
            val list = info.includePatterns
            if (list.isNotEmpty()) {
                val exlist = info.excludePatterns
                var pattern: Pattern
                var p: Pattern
                for(i in 0 until list.size) {
                    pattern = list[i]
                    if (pattern.matcher(entryName).matches()) {
                        if (exlist.isNotEmpty()) {
                            for(j in 0 until exlist.size) {
                                p = exlist[j]
                                if (p.matcher(entryName).matches())
                                    return false
                            }
                        }
                        return true
                    }
                }
            }
            return false
        }
    }
}