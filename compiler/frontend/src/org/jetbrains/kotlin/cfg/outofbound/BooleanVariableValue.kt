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

package org.jetbrains.kotlin.cfg.outofbound

import org.jetbrains.kotlin.descriptors.VariableDescriptor

public sealed class BooleanVariableValue {
    // Logic operators, (BoolVariableValue, BoolVariableValue) -> BoolVariableValue
    public abstract fun and(other: BooleanVariableValue): BooleanVariableValue
    public abstract fun or(other: BooleanVariableValue): BooleanVariableValue
    public abstract fun not(): BooleanVariableValue

    public abstract fun merge(other: BooleanVariableValue): BooleanVariableValue

    // For now derived classes of BooleanVariableValue are immutable,
    // so copy returns this. In the future, if some class become mutable
    // the implementation of this method may change
    public fun copy(): BooleanVariableValue = this

    public object True : BooleanVariableValue() {
        override fun toString(): String = "T"

        override fun and(other: BooleanVariableValue): BooleanVariableValue = other.copy()

        override fun or(other: BooleanVariableValue): BooleanVariableValue = True

        override fun not(): BooleanVariableValue = False

        override fun merge(other: BooleanVariableValue): BooleanVariableValue =
                when (other) {
                    True -> True
                    False -> Undefined.WITH_NO_RESTRICTIONS
                    is Undefined -> Undefined(Restrictions.Empty, other.onFalseRestrictions)
                }
    }

    public object False : BooleanVariableValue() {
        override fun toString(): String = "F"

        override fun and(other: BooleanVariableValue): BooleanVariableValue = False

        override fun or(other: BooleanVariableValue): BooleanVariableValue = other.copy()

        override fun not(): BooleanVariableValue = True

        override fun merge(other: BooleanVariableValue): BooleanVariableValue =
                when (other) {
                    True -> Undefined.WITH_NO_RESTRICTIONS
                    False -> False
                    is Undefined -> Undefined(other.onTrueRestrictions, Restrictions.Empty)
                }
    }

    public data class Undefined (
            val onTrueRestrictions: Restrictions,
            val onFalseRestrictions: Restrictions
    ): BooleanVariableValue() {
        override fun toString(): String = "U${onTrueRestrictions.toString()}${onFalseRestrictions.toString()}"

        override fun and(other: BooleanVariableValue): BooleanVariableValue =
                when(other) {
                    True -> this.copy()
                    False -> False
                    is Undefined -> Undefined(
                            this.onTrueRestrictions.andInTruePosition(other.onTrueRestrictions),
                            this.onFalseRestrictions.andInFalsePosition(other.onFalseRestrictions)
                    )
                }

        override fun or(other: BooleanVariableValue): BooleanVariableValue =
                when(other) {
                    True -> True
                    False -> this.copy()
                    is Undefined -> Undefined(
                            this.onTrueRestrictions.orInTruePosition(other.onTrueRestrictions),
                            this.onFalseRestrictions.orInFalsePosition(other.onFalseRestrictions)
                    )
                }

        override fun not(): BooleanVariableValue = Undefined(onFalseRestrictions, onTrueRestrictions)

        override fun merge(other: BooleanVariableValue): BooleanVariableValue =
                when (other) {
                    True -> Undefined(Restrictions.Empty, this.onFalseRestrictions)
                    False -> Undefined(this.onTrueRestrictions, Restrictions.Empty)
                    is Undefined -> Undefined(
                            this.onTrueRestrictions.mergeInTruePosition(other.onTrueRestrictions),
                            this.onFalseRestrictions.mergeInFalsePosition(other.onFalseRestrictions)
                    )
                }

        companion object {
            public val WITH_NO_RESTRICTIONS: Undefined = Undefined(Restrictions.Empty, Restrictions.Empty)
        }
    }

    companion object {
        public fun create(value: Boolean): BooleanVariableValue = if(value) True else False
    }
}

public sealed class Restrictions {
    public abstract fun andInTruePosition(other: Restrictions): Restrictions
    public abstract fun andInFalsePosition(other: Restrictions): Restrictions

    public abstract fun orInTruePosition(other: Restrictions): Restrictions
    public abstract fun orInFalsePosition(other: Restrictions): Restrictions

    public abstract fun mergeInTruePosition(other: Restrictions): Restrictions
    public abstract fun mergeInFalsePosition(other: Restrictions): Restrictions

    public object Empty : Restrictions() {
        override fun toString(): String = "{}"

        override fun andInTruePosition(other: Restrictions): Restrictions = other
        override fun andInFalsePosition(other: Restrictions): Restrictions = Empty

        override fun orInTruePosition(other: Restrictions): Restrictions = Empty
        override fun orInFalsePosition(other: Restrictions): Restrictions = other

        override fun mergeInTruePosition(other: Restrictions): Restrictions = Empty
        override fun mergeInFalsePosition(other: Restrictions): Restrictions = Empty
    }

    public data class Specific protected constructor(val values: Map<VariableDescriptor, Set<Int>>) : Restrictions() {
        override fun toString(): String {
            val descriptorToString: (VariableDescriptor) -> String = { it.name.asString() }
            val setToString: (Set<Int>) -> String = { it.sort().toString() }
            return MapUtils.mapToString(values, descriptorToString, descriptorToString, setToString)
        }

        override fun andInTruePosition(other: Restrictions): Restrictions =
                when (other) {
                    Empty -> this
                    is Specific -> Specific(MapUtils.intersectMaps(this.values, other.values) { v1, v2 -> v1 intersect v2 })
                }

        override fun andInFalsePosition(other: Restrictions): Restrictions =
                when (other) {
                    Empty -> Empty
                    is Specific -> Specific(MapUtils.unionMaps(this.values, other.values) { v1, v2 -> v1 union v2 })
                }

        override fun orInTruePosition(other: Restrictions): Restrictions = andInFalsePosition(other)
        override fun orInFalsePosition(other: Restrictions): Restrictions = andInTruePosition(other)

        override fun mergeInTruePosition(other: Restrictions): Restrictions = andInFalsePosition(other)
        override fun mergeInFalsePosition(other: Restrictions): Restrictions = andInFalsePosition(other)

        companion object {
            public fun create(values: Map<VariableDescriptor, Set<Int>>): Restrictions =
                    if (values.isEmpty()) Empty else Specific(values)
        }
    }
}