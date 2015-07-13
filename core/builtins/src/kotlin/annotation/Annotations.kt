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

package kotlin.annotation

/**
 * Contains the list of possible annotation targets
 */
public enum class AnnotationTarget {
    /** Package directive */
    PACKAGE,
    /** class, interface or object, annotation class is also included */
    CLASSIFIER,
    /** annotation class only */
    ANNOTATION_CLASS,
    /** generic type parameter (unsupported yet) */
    TYPE_PARAMETER,
    /** property */
    PROPERTY,
    /** field, including property's backing field */
    FIELD,
    /** local variable */
    LOCAL_VARIABLE,
    /** value parameter of a function */
    VALUE_PARAMETER,
    /** constructor only (primary or secondary) */
    CONSTRUCTOR,
    /** function (constructors are not included */
    FUNCTION,
    /** property getter only */
    PROPERTY_GETTER,
    /** property setter only */
    PROPERTY_SETTER,
    /** type of a variable / property / value parameter / return type */
    TYPE,
    /** any expression */
    EXPRESSION,
    /** file */
    FILE
}

/**
 * Contains the list of possible annotation's retention
 *
 * Determined how an annotation is stored in class file.
 */
public enum class AnnotationRetention {
    /** Annotation isn't stored in a class file */
    SOURCE,
    /** Annotation is stored in a class file, but invisible for reflection */
    BINARY,
    /** Annotation is stored in a class file and visible for reflection */
    RUNTIME
}

/**
 * This annotation determines a possible target of another annotation
 *
 * [allowedTargets] list of allowed annotation targets
 */
target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class target(vararg val allowedTargets: AnnotationTarget)

/**
 * This special annotation is used to declare class as an annotation and to set its base properties
 *
 * [retention] determines whether the annotation is stored in a class file and visible for reflection. By default, both are true.
 * [repeatable] true if annotation is repeatable (applicable twice or more on a single code object), otherwise false (default)
 */
target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class annotation (
        val retention: AnnotationRetention = AnnotationRetention.RUNTIME,
        val repeatable: Boolean = false
)
