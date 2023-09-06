package com.cnoke.register

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class CodeInsertProcessor private constructor(val extension: RegisterInfo) {

    companion object {
         fun insertInitCodeTo(extension: RegisterInfo) {
            if (extension.classList.isNotEmpty()) {
                val processor = CodeInsertProcessor(extension)
                extension.fileContainsInitClassPath.let {
                    if (it.endsWith(".jar"))
                        processor.generateCodeIntoJarFile(File(it))
                    else
                        processor.generateCodeIntoClassFile(File(it))
                }
            }
        }
    }



    //处理jar包中的class代码注入
    private fun generateCodeIntoJarFile(jarFile: File): File {
        if (jarFile.exists()) {
            val optJar =  File(jarFile.parent, jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            val file = JarFile(jarFile)
            val enumeration = file.entries()
            val jarOutputStream =  JarOutputStream(FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement() as JarEntry
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (isInitClass(entryName)) {
                    println("generate code into:$entryName")
                    val bytes = doGenerateCode(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

    fun isInitClass(entryName: String): Boolean {
        if (!entryName.endsWith(".class"))
            return false
        if (extension.initClassName.isNotEmpty()) {
            val newEntryName = entryName.substring(0, entryName.lastIndexOf('.'))
            return extension.initClassName == newEntryName
        }
        return false
    }
    /**
     * 处理class的注入
     * @param file class文件
     * @return 修改后的字节码文件内容
     */
    private fun generateCodeIntoClassFile(file: File): ByteArray {
        val optClass = File(file.parent, file.name + ".opt")

        val inputStream =  FileInputStream(file)
        val outputStream =  FileOutputStream(optClass)

        val bytes = doGenerateCode(inputStream)
        outputStream.write(bytes)
        inputStream.close()
        outputStream.close()
        if (file.exists()) {
            file.delete()
        }
        optClass.renameTo(file)
        return bytes
    }

    private fun doGenerateCode(inputStream: InputStream): ByteArray {
        val cr =  ClassReader(inputStream)
        val cw =  ClassWriter(cr, 0)
        val cv =  MyClassVisitor(Opcodes.ASM6, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    inner class MyClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if(name == extension.initMethodName) { //注入代码到指定的方法之中
                val _static = (access and Opcodes.ACC_STATIC) > 0
                mv = MyMethodVisitor(Opcodes.ASM6, mv, _static)
                println(" start visit class: ${extension.initClassName} ")
            }
            return mv
        }

    }

    inner class MyMethodVisitor(api: Int, mv: MethodVisitor, var _static: Boolean) : MethodVisitor(api, mv) {

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                extension.classList.forEach { name ->
                    println(" start visitInsn name: $name, isStatic: $_static, isInstance: ${extension.isInstance}")
                    if (!_static) {
                        //加载this
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                    }
                    if(extension.isInstance){
                        //用kotlin单例创建一个组件实例
                        val companion = name + "\$Companion"
                        mv.visitFieldInsn(Opcodes.GETSTATIC, name, "Companion", "L${companion};")
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, companion, "getInstance", "()L${name};", false)
                    }else{
                        //用无参构造方法创建一个组件实例
                        mv.visitTypeInsn(Opcodes.NEW, name)
                        mv.visitInsn(Opcodes.DUP)
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
                    }
                    //调用注册方法将组件实例注册到组件库中
                    if (_static) {
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC
                            , extension.registerClassName
                            , extension.registerMethodName
                            , "(L${extension.interfaceName};)V"
                            , false)
                    } else {
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
                            , extension.registerClassName
                            , extension.registerMethodName
                            , "(L${extension.interfaceName};)V"
                            , false)
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}