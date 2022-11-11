package com.cnoke.registeraction

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.*


abstract class CodeScanTransform : AsmClassVisitorFactory<RegisterConfigParams> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
           println("createClassVisitor CodeScanTransform")
        return ScanClassVisitor(parameters.get().registerConfig.get(), Opcodes.ASM9, nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val registerInfoList = parameters.get().registerConfig.get().list
//        registerInfoList.forEach {
//            if(it.initClassName == classData.className){
//                return true
//            }
//        }
    //    println(" scan interfaces ${classData.interfaces.toTypedArray().contentToString()} ")
        classData.interfaces.forEach { superInterface ->
            registerInfoList.forEach {
                if(it.interfaceName.startsWith(superInterface)){
                    println(" isInstrumentable true ${classData.className} ")
                    return true
                }
            }
        }
        return false
    }
}

class ScanClassVisitor(private val registerConfig: AutoRegisterConfigData, api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
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
        println(" scan class: $name,  interfaces ${interfaces.contentToString()} ")
        interfaces?.forEach { superInterface ->
            registerConfig.list.forEach {
                if(RegisterInfoData.convertSlashToDot(superInterface) == it.interfaceName) {
                    AutoRegisterHelper.classArray.add(name!!)
                    println("find need to insertClass $name")
                    return@forEach
                }
            }
        }
    }
}