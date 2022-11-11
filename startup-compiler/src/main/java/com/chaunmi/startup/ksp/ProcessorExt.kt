package com.chaunmi.startup.ksp

import com.google.devtools.ksp.symbol.KSDeclaration
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

fun KSDeclaration.toClassName(): String {
    return packageName.asString() +  "." + simpleName.asString()
}

fun getProcessID(): String {
    val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
    return runtimeMXBean.name + "@" + Integer.valueOf(runtimeMXBean.name.split("@")[0]).toInt()
}