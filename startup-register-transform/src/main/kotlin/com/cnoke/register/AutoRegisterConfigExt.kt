package com.cnoke.register

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface AutoRegisterConfigExt {
    val cacheEnabled: Property<Boolean>
    val registerInfo: ListProperty<Map<String, Any>>
}