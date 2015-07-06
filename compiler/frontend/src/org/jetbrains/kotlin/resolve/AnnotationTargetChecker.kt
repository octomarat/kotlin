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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getAnnotationEntries
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.types.JetType
import org.jetbrains.kotlin.types.TypeUtils

public class AnnotationTargetChecker private constructor() {

    private val DEFAULT_TARGETS = listOf("PACKAGE", "CLASSIFIER", "ANNOTATION_CLASS", "FIELD", "LOCAL_VARIABLE", "PROPERTY",
                                         "VALUE_PARAMETER", "CONSTRUCTOR", "FUNCTION", "PROPERTY_GETTER", "PROPERTY_SETTER")


    public fun checkDeclaration(declaration: JetDeclaration, trace: BindingTrace) {
        val actualTarget = getActualTarget(declaration)
        for (entry in declaration.getAnnotationEntries()) {
            checkAnnotationEntry(entry, actualTarget, trace)
        }
        if (declaration is JetCallableDeclaration) {
            declaration.getTypeReference()?.let { checkTypeReference(it, trace) }
        }
        if (declaration is JetFunction) {
            for (parameter in declaration.getValueParameters()) {
                checkDeclaration(parameter, trace)
            }
        }
    }

    public fun checkExpression(expression: JetExpression, trace: BindingTrace) {
        for (entry in expression.getAnnotationEntries()) {
            checkAnnotationEntry(entry, "EXPRESSION", trace)
        }
    }

    public fun checkPackageDirective(directive: JetPackageDirective, trace: BindingTrace) {
        for (entry in directive.getAnnotationEntries()) {
            checkAnnotationEntry(entry, "PACKAGE", trace)
        }
    }

    public fun checkTypeParameter(typeParameter: JetTypeParameter, trace: BindingTrace) {
        // TODO: unsupported yet
//        for (entry in typeParameter.getAnnotationEntries()) {
//            checkAnnotationEntry(entry, "TYPE_PARAMETER", trace)
//        }
    }

    public fun checkTypeReference(typeReference: JetTypeReference, trace: BindingTrace) {
        for (entry in typeReference.getAnnotationEntries()) {
            checkAnnotationEntry(entry, "TYPE", trace)
        }
    }

    public fun checkFile(file: JetFile, trace: BindingTrace) {
        for (entry in file.getAnnotationEntries()) {
            checkAnnotationEntry(entry, "FILE", trace)
        }
    }

    private fun checkAnnotationEntry(entry: JetAnnotationEntry, actualTarget: String, trace: BindingTrace) {
        val descriptor = trace.get(BindingContext.ANNOTATION, entry)
        val type = descriptor?.getType()
        val classDescriptor = type?.let { TypeUtils.getClassDescriptor(it) }
        val targetEntryDescriptor = classDescriptor?.getAnnotations()?.findAnnotation(KotlinBuiltIns.FQ_NAMES.target)
        val valueArguments = targetEntryDescriptor?.getAllValueArguments()
        val valueArgument = if (valueArguments?.isEmpty() == false) valueArguments!!.iterator().next().getValue() as? ArrayValue else null
        val possibleTargets = valueArgument?.getValue()?.map {
            val target = it.toString()
            target.substring(target.lastIndexOf('.') + 1)
        } ?: DEFAULT_TARGETS
        if (!possibleTargets.contains(actualTarget)) {
            trace.report(Errors.WRONG_ANNOTATION_TARGET.on(entry, actualTarget))
        }
    }

    private fun getActualTarget(declaration: JetDeclaration): String {
        if (declaration is JetClassOrObject) {
            return if (declaration.isAnnotation()) "ANNOTATION_CLASS" else "CLASSIFIER"
        }
        if (declaration is JetProperty) {
            return if (declaration.isLocal()) "LOCAL_VARIABLE" else "PROPERTY"
        }
        if (declaration is JetParameter) {
            return if (declaration.hasValOrVar()) "PROPERTY" else "VALUE_PARAMETER"
        }
        if (declaration is JetConstructor<*>) return "CONSTRUCTOR"
        if (declaration is JetFunction) return "FUNCTION"
        if (declaration is JetPropertyAccessor) {
            return if (declaration.isGetter()) "PROPERTY_GETTER" else "PROPERTY_SETTER"
        }
        // TODO: other types
        return "XXX"
    }

    companion object {
        public val instance: AnnotationTargetChecker = AnnotationTargetChecker()
    }
}