/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.MemberDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.scopes.ChainedScope
import org.jetbrains.kotlin.resolve.scopes.JetScope
import org.jetbrains.kotlin.serialization.deserialization.findClassAcrossModuleDependencies
import kotlin.reflect.KClass
import kotlin.reflect.KElement
import kotlin.reflect.KotlinReflectionInternalError

class KClassImpl<T>(override val jClass: Class<T>) : KCallableContainerImpl(), KClass<T> {
    // Don't use kotlin.properties.Delegates here because it's a Kotlin class which will invoke KClassImpl() in <clinit>,
    // resulting in infinite recursion

    val descriptor by ReflectProperties.lazySoft {
        val classId = classId

        val descriptor =
                if (classId.isLocal()) moduleData.localClassResolver.resolveLocalClass(classId)
                else moduleData.module.findClassAcrossModuleDependencies(classId)

        descriptor ?: throw KotlinReflectionInternalError("Class not resolved: $jClass")
    }

    private val classId: ClassId get() = RuntimeTypeMapper.mapJvmClassToKotlinClassId(jClass)

    override val scope: JetScope get() = ChainedScope(
            descriptor, "KClassImpl scope", descriptor.getDefaultType().getMemberScope(), descriptor.getStaticScope()
    )

    override val simpleName: String? get() {
        if (jClass.isAnonymousClass()) return null

        val classId = classId
        return when {
            classId.isLocal() -> calculateLocalClassName(jClass)
            else -> classId.getShortClassName().asString()
        }
    }

    private fun calculateLocalClassName(jClass: Class<*>): String {
        val name = jClass.getSimpleName()
        jClass.getEnclosingMethod()?.let { method ->
            return name.substringAfter(method.getName() + "$")
        }
        jClass.getEnclosingConstructor()?.let { constructor ->
            return name.substringAfter(constructor.getName() + "$")
        }
        return name.substringAfter('$')
    }

    override val qualifiedName: String? get() {
        if (jClass.isAnonymousClass()) return null

        val classId = classId
        return when {
            classId.isLocal() -> null
            else -> classId.asSingleFqName().asString()
        }
    }

    override val members: Collection<KElement>
        get() = getMembers(declaredOnly = false, nonExtensions = true, extensions = true).toList()

    fun getMembers(declaredOnly: Boolean, nonExtensions: Boolean, extensions: Boolean): Sequence<KElement> =
            scope.getAllDescriptors().asSequence()
                    .filter { descriptor ->
                        descriptor !is MemberDescriptor || descriptor.getVisibility() != Visibilities.INVISIBLE_FAKE
                    }
                    .map { descriptor ->
                        descriptor.accept(object : DeclarationDescriptorVisitorEmptyBodies<KElement?, Nothing>() {
                            override fun visitPropertyDescriptor(descriptor: PropertyDescriptor, data: Nothing?): KElement? {
                                if (declaredOnly && !descriptor.getKind().isReal()) return null

                                val isExtension = descriptor.getExtensionReceiverParameter() != null
                                if (isExtension && !extensions) return null
                                if (!isExtension && !nonExtensions) return null

                                return if (!isExtension) {
                                    if (descriptor.isVar()) KMutableProperty1Impl<T, Any?>(this@KClassImpl, descriptor)
                                    else KProperty1Impl<T, Any?>(this@KClassImpl, descriptor)
                                }
                                else {
                                    if (descriptor.isVar()) KMutableProperty2Impl<T, Any?, Any?>(this@KClassImpl, descriptor)
                                    else KProperty2Impl<T, Any?, Any?>(this@KClassImpl, descriptor)
                                }
                            }
                        }, null)
                    }
                    .filterNotNull()

    override fun equals(other: Any?): Boolean =
            other is KClassImpl<*> && jClass == other.jClass

    override fun hashCode(): Int =
            jClass.hashCode()

    override fun toString(): String {
        return "class " + classId.let { classId ->
            val packageFqName = classId.getPackageFqName()
            val packagePrefix = if (packageFqName.isRoot()) "" else packageFqName.asString() + "."
            val classSuffix = classId.getRelativeClassName().asString().replace('.', '$')
            packagePrefix + classSuffix
        }
    }
}
