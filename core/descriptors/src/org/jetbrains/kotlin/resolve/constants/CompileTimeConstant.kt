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

package org.jetbrains.kotlin.resolve.constants

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.annotations.AnnotationArgumentVisitor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.types.*

public interface CompileTimeConstant<out T> {
    public val isError: Boolean
        get() = false

    public val parameters: CompileTimeConstant.Parameters

    public fun toStrictlyTyped(expectedType: JetType): ConstantValueCompileTimeConstant<T>

    public fun toConstantValue(expectedType: JetType): ConstantValue<T> = toStrictlyTyped(expectedType).constantValue

    public fun getValue(expectedType: JetType): T

    public fun canBeUsedInAnnotations(): Boolean = parameters.canBeUsedInAnnotation

    public fun usesVariableAsConstant(): Boolean = parameters.usesVariableAsConstant

    public class Parameters(
            public val canBeUsedInAnnotation: Boolean,
            public val isPure: Boolean,
            public val usesVariableAsConstant: Boolean
    )
}

public class ConstantValueCompileTimeConstant<out T>(
        public val constantValue: ConstantValue<T>,
        override val parameters: CompileTimeConstant.Parameters
) : CompileTimeConstant<T> {
    override val isError: Boolean
        get() = constantValue is ErrorValue

    override fun toStrictlyTyped(expectedType: JetType) = this

    public val type: JetType = constantValue.type

    override fun getValue(expectedType: JetType) = constantValue.value
}

public fun <T> ConstantValue<T>.wrap(parameters: CompileTimeConstant.Parameters = defaultParameters()): ConstantValueCompileTimeConstant<T>
        = ConstantValueCompileTimeConstant(this, parameters)

public fun ConstantValue<*>.defaultParameters(): CompileTimeConstant.Parameters = CompileTimeConstant.Parameters(
        canBeUsedInAnnotation = this !is NullValue,
        isPure = false,
        usesVariableAsConstant = false
)

public class IntegerValueTypeConstant(
        private val value: Number,
        override val parameters: CompileTimeConstant.Parameters
) : CompileTimeConstant<Number> {
    private val typeConstructor = IntegerValueTypeConstructor(value.toLong())

    override fun toStrictlyTyped(expectedType: JetType): ConstantValueCompileTimeConstant<Number> {
        val factory = CompileTimeConstantFactory(KotlinBuiltIns.getInstance())
        val constantValue: ConstantValue<Number> = when (getType(expectedType)) {
            KotlinBuiltIns.getInstance().getIntType() -> {
                factory.createIntValue(value.toInt())
            }
            KotlinBuiltIns.getInstance().getByteType() -> {
                factory.createByteValue(value.toByte())
            }
            KotlinBuiltIns.getInstance().getShortType() -> {
                factory.createShortValue(value.toShort())
            }
            else -> {
                factory.createLongValue(value.toLong())
            }
        }
        //TODO_R: parameters?
        return ConstantValueCompileTimeConstant(constantValue, parameters)
    }

    val unknownIntegerType = JetTypeImpl(
            Annotations.EMPTY, typeConstructor, false, emptyList<TypeProjection>(),
            ErrorUtils.createErrorScope("Scope for number value type (" + typeConstructor.toString() + ")", true)
    )

    public fun getType(expectedType: JetType): JetType = TypeUtils.getPrimitiveNumberType(typeConstructor, expectedType)

    override fun getValue(expectedType: JetType): Number {
        return toStrictlyTyped(expectedType).constantValue.value
    }

    override fun toString() = typeConstructor.toString()
}
