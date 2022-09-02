package com.cnoke.registeraction

import com.android.build.api.instrumentation.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 注意参数必须是可Serialization
 * @property registerConfig Property<AutoRegisterConfigData>
 */
interface RegisterConfigParams : InstrumentationParameters {
    @get:Input
    val registerConfig: Property<AutoRegisterConfigData>
}

abstract class CodeInsertTransform: AsmClassVisitorFactory<RegisterConfigParams> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
    //   println("createClassVisitor CodeInsertTransform")
       return InsertClassVisitor(parameters.get().registerConfig.get(), Opcodes.ASM9, nextClassVisitor)
    }

    /**
     * 这里的className为.
     * @param classData ClassData
     * @return Boolean
     */
    override fun isInstrumentable(classData: ClassData): Boolean {
        val registerInfoList = parameters.get().registerConfig.get().list
        registerInfoList.forEach {
            if(it.initClassName == classData.className){
                return true
            }
        }
//        classData.interfaces.forEach { superInterface ->
//            registerInfoList.forEach {
//                if(it.interfaceName.startsWith(superInterface)){
//                    return true
//                }
//            }
//        }
        return false
    }
}

 class InsertClassVisitor(private val registerConfig: AutoRegisterConfigData, api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
     private var registerInfo: RegisterInfoData? = null



     /**
      * 注意这里的name是带斜杠的
      * @param version Int
      * @param access Int
      * @param name String
      * @param signature String
      * @param superName String
      * @param interfaces Array<out String>
      */
     override fun visit(
         version: Int,
         access: Int,
         name: String?,
         signature: String?,
         superName: String?,
         interfaces: Array<out String>?
     ) {
         super.visit(version, access, name, signature, superName, interfaces)
         //抽象类、接口、非public等类无法调用其无参构造方法
         if (access and Opcodes.ACC_ABSTRACT != 0
             || (access and Opcodes.ACC_INTERFACE != 0)
             || access and Opcodes.ACC_PUBLIC == 0
         ) {
             return
         }
         registerConfig.list.forEach {
            if(RegisterInfoData.convertSlashToDot(name) == it.initClassName) {
                registerInfo = it
                println("find insertClass $name")
                return@forEach
            }
         }
     }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)

        registerInfo?.apply {
            val isAbstractMethod = access and Opcodes.ACC_ABSTRACT != 0
            val isNativeMethod = access and Opcodes.ACC_NATIVE != 0
            val isStaticInitMethod = "<init>" == name || "<clinit>" == name
            if(isAbstractMethod || isNativeMethod || isStaticInitMethod) {
                return mv
            }
            if(name == initMethodName) { //注入代码到指定的方法之中
                val isStatic = (access and Opcodes.ACC_STATIC) > 0
                println(" find insetMethodName: $name")
                mv = InsertMethodVisitor(this, Opcodes.ASM9, mv, isStatic)
            }
        }
        return mv
    }

}

 class InsertMethodVisitor(private val registerInfo: RegisterInfoData, api: Int, mv: MethodVisitor, var _static: Boolean) : MethodVisitor(api, mv) {

    override fun visitInsn(opcode: Int) {
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            println(" insert register code to class ${registerInfo.initClassName} ")
            registerInfo.classList.forEach { name ->
                if (!_static) {
                    //加载this
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                }
                if(registerInfo.isInstance){
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
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC
                        , registerInfo.registerClassName
                        , registerInfo.registerMethodName
                        , "(L${registerInfo.interfaceName};)V"
                        , false)
                } else {
                    mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL
                        , registerInfo.registerClassName
                        , registerInfo.registerMethodName
                        , "(L${registerInfo.interfaceName};)V"
                        , false)
                }
                println(" insert class: $name ")
            }
        }
        super.visitInsn(opcode)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack + 4, maxLocals)
    }
}