package org.jetbrains.kotlin.cfg.valuesanalysis

import org.jetbrains.kotlin.descriptors.VariableDescriptor
import java.util.HashSet

public sealed class IntegerVariableValue {
    public abstract fun merge(other: IntegerVariableValue): IntegerVariableValue
    public fun copy(): IntegerVariableValue = this // if some subclass is mutable, it should override this method

    // operators
    public open fun plus(others: IntegerVariableValue): IntegerVariableValue = IntegerVariableValue.Undefined
    public open fun minus(others: IntegerVariableValue): IntegerVariableValue = IntegerVariableValue.Undefined
    public open fun times(others: IntegerVariableValue): IntegerVariableValue = IntegerVariableValue.Undefined
    public open fun div(others: IntegerVariableValue): IntegerVariableValue = IntegerVariableValue.Undefined
    public open fun rangeTo(others: IntegerVariableValue): IntegerVariableValue = IntegerVariableValue.Undefined
    public open fun minus(): IntegerVariableValue = IntegerVariableValue.Undefined

    // special operators (IntegerValues, IntegerValues) -> BooleanVariableValue
    public open fun eq(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
    public open fun notEq(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
    public open fun lessThan(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
    public open fun greaterThan(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
    public open fun greaterOrEq(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
    public open fun lessOrEq(other: IntegerVariableValue, thisVarDescriptor: VariableDescriptor?)
            : BooleanVariableValue = BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS

    // Represents a value of Integer variable that is not initialized
    public object Uninitialized : IntegerVariableValue() {
        override fun toString(): String = "-"

        override fun merge(other: IntegerVariableValue): IntegerVariableValue =
                when (other) {
                    is IntegerVariableValue.Defined -> other.merge(this)
                    else -> other
                }
    }

    // Represents a value of Integer variable that is obtained from constructions analysis don't process
    // (for ex, in `a = foo()` `a` has an Undefined value, because function calls are not processed)
    public object Undefined : IntegerVariableValue() {
        override fun toString(): String = "?"

        override fun merge(other: IntegerVariableValue): IntegerVariableValue =
                when (other) {
                    is IntegerVariableValue.Defined -> other.merge(this)
                    else -> Undefined
                }
    }

    // Represent a set of values Integer variable can have
    public class Defined private constructor(
            possibleValues: Set<Int>,
            forceNotAllValuesKnown: Boolean = false
    ) : IntegerVariableValue() {
        companion object {
            private val POSSIBLE_VALUES_THRESHOLD = 2
        }

        public val values: Set<Int>
        // `values` set may contain no more than `POSSIBLE_VALUES_THRESHOLD` values.
        // If there was attempt to add more values they would not be added and the flag below would be set
        public val allPossibleValuesKnown: Boolean

        init {
            assert(POSSIBLE_VALUES_THRESHOLD > 0, "Possible values threshold must be positive number")
            assert(!possibleValues.isEmpty(), "IntegerVariableValue.Defined must contain at least one value")
            this.allPossibleValuesKnown = if (forceNotAllValuesKnown) false else possibleValues.size() <= POSSIBLE_VALUES_THRESHOLD
            this.values =
                if (this.allPossibleValuesKnown)
                    possibleValues
                else {
                    val original = possibleValues.toSortedList()
                    val elementsToTake = (POSSIBLE_VALUES_THRESHOLD + 1) / 2
                    val minValues = original.take(elementsToTake)
                    val maxValues = original.reverse().take(elementsToTake)
                    if (POSSIBLE_VALUES_THRESHOLD % 2 == 1)
                        (maxValues + minValues.dropLast(1)).toSet()
                    else
                        (maxValues + minValues).toSet()
                }
        }

        public constructor(possibleValue: Int) : this(setOf(possibleValue))

        override fun equals(other: Any?): Boolean {
            return this === other ||
                   other is Defined &&
                   this.allPossibleValuesKnown == other.allPossibleValuesKnown &&
                   this.values == other.values
        }

        override fun hashCode(): Int {
            var code = 7
            code = 31 * code + this.allPossibleValuesKnown.hashCode()
            code = 31 * code + this.values.hashCode()
            return code
        }

        override fun toString(): String {
            val listAsString = "${this.values.toSortedList().toString()}"
            if (this.allPossibleValuesKnown) {
                return listAsString
            }
            return "${listAsString.dropLast(1)}, ?]"
        }

        override fun merge(other: IntegerVariableValue): IntegerVariableValue =
            when (other) {
                is IntegerVariableValue.Uninitialized,
                is IntegerVariableValue.Undefined -> this.toValueWithNotAllPossibleValuesKnown()
                is Defined -> {
                    val notAllValuesKnown = !this.allPossibleValuesKnown || !other.allPossibleValuesKnown
                    Defined(this.values + other.values, forceNotAllValuesKnown = notAllValuesKnown)
                }
            }

        public fun leaveOnlyValuesInSet(valuesToLeave: Set<Int>): IntegerVariableValue {
            val intersection = this.values.intersect(valuesToLeave)
            return if (intersection.isEmpty()) Undefined
            else Defined(intersection, forceNotAllValuesKnown = !this.allPossibleValuesKnown)
        }

        public fun toValueWithNotAllPossibleValuesKnown(): Defined = Defined(this.values, forceNotAllValuesKnown = true)

        // operators overloading
        override fun plus(others: IntegerVariableValue): IntegerVariableValue = applyEachToEach(others) { x, y -> x + y }
        override fun minus(others: IntegerVariableValue): IntegerVariableValue = applyEachToEach(others) { x, y -> x - y }
        override fun times(others: IntegerVariableValue): IntegerVariableValue = applyEachToEach(others) { x, y -> x * y }

        override fun div(others: IntegerVariableValue): IntegerVariableValue =
                if (others !is Defined)
                    IntegerVariableValue.Undefined
                else {
                    val nonZero = others.values.filter { it != 0 }.toSet()
                    if (nonZero.isEmpty())
                        IntegerVariableValue.Undefined
                    else {
                        val noneZeroOthers = Defined(nonZero, forceNotAllValuesKnown = !others.allPossibleValuesKnown)
                        applyEachToEach(noneZeroOthers) { x, y -> x / y }
                    }
                }

        override fun rangeTo(others: IntegerVariableValue): IntegerVariableValue =
            if (others !is Defined)
                IntegerVariableValue.Undefined
            else {
                val minOfLeftOperand = this.values.min() as Int
                val maxOfRightOperand = others.values.max() as Int
                val rangeValues = hashSetOf<Int>()
                for (value in minOfLeftOperand..maxOfRightOperand) { rangeValues.add(value) }
                if (rangeValues.isEmpty())
                    IntegerVariableValue.Undefined
                else
                    Defined(rangeValues, forceNotAllValuesKnown = !this.allPossibleValuesKnown || !others.allPossibleValuesKnown)
            }

        override fun minus(): IntegerVariableValue {
            val valuesSet = this.values.map { -1 * it }.toSet()
            return Defined(valuesSet, forceNotAllValuesKnown = !this.allPossibleValuesKnown)
        }

        private fun applyEachToEach(other: IntegerVariableValue, operation: (Int, Int) -> Int): IntegerVariableValue =
                if (other is Defined) {
                    val results = this.values
                            .map { leftOp -> other.values.map { rightOp -> operation(leftOp, rightOp) } }
                            .flatten()
                            .toSet()
                    Defined(results, forceNotAllValuesKnown = !this.allPossibleValuesKnown || !other.allPossibleValuesKnown)
                }
                else IntegerVariableValue.Undefined

        override fun eq(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue {
            // the second check means that in expression "x 'operator' y" only one element set is supported for "y"
            if (other is Defined && other.values.size() == 1) {
                val valueToCompareWith = other.values.single()
                val thisValues = HashSet(values)
                val onTrueValues = if (thisValues.contains(valueToCompareWith)) setOf(valueToCompareWith) else setOf()
                thisValues.remove(valueToCompareWith)
                if (this.allPossibleValuesKnown) {
                    if (onTrueValues.isEmpty()) {
                        return BooleanVariableValue.False
                    }
                    else if (thisValues.isEmpty()) {
                        return BooleanVariableValue.True
                    }
                }
                return thisVarDescriptor?.let {
                    BooleanVariableValue.Undefined(
                            Restrictions.Specific.create(mapOf(it to onTrueValues)),
                            Restrictions.Specific.create(mapOf(it to thisValues))
                    )
                } ?: BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
            }
            else return applyFullRestrictionsOnThisValuesIfPossible(thisVarDescriptor)
        }

        override fun notEq(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue =
                !eq(other, thisVarDescriptor)

        override fun lessThan(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue =
                // the second check means that in expression "x 'operator' y" only one element set is supported for "y"
                if (other is Defined && other.values.size() == 1) comparison(
                        other.values.single(),
                        { array, value -> array.indexOfFirst { it >= value } },
                        { valuesLessThanOther, valuesGreaterOrEqToOther ->
                            if (this.allPossibleValuesKnown) {
                                if (valuesLessThanOther.isEmpty()) {
                                    return@comparison BooleanVariableValue.False
                                }
                                else if (valuesGreaterOrEqToOther.isEmpty()) {
                                    return@comparison BooleanVariableValue.True
                                }
                            }
                            return@comparison thisVarDescriptor?.let {
                                BooleanVariableValue.Undefined(
                                        Restrictions.Specific.create(mapOf(it to valuesLessThanOther)),
                                        Restrictions.Specific.create(mapOf(it to valuesGreaterOrEqToOther))
                                )
                            } ?: BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
                        }
                )
                else applyFullRestrictionsOnThisValuesIfPossible(thisVarDescriptor)

        override fun greaterThan(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue =
                // the second check means that in expression "x 'operator' y" only one element set is supported for "y"
                if (other is Defined && other.values.size() == 1) comparison(
                        other.values.single(),
                        { array, value -> array.indexOfFirst { it > value } },
                        { valuesLessOrEqToOther, valuesGreaterThanOther ->
                            if (this.allPossibleValuesKnown) {
                                if (valuesLessOrEqToOther.isEmpty()) {
                                    return@comparison BooleanVariableValue.True
                                }
                                else if (valuesGreaterThanOther.isEmpty()) {
                                    return@comparison BooleanVariableValue.False
                                }
                            }
                            return@comparison thisVarDescriptor?.let {
                                BooleanVariableValue.Undefined(
                                        Restrictions.Specific.create(mapOf(it to valuesGreaterThanOther)),
                                        Restrictions.Specific.create(mapOf(it to valuesLessOrEqToOther))
                                )
                            } ?: BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS
                        }
                )
                else applyFullRestrictionsOnThisValuesIfPossible(thisVarDescriptor)

        private fun applyFullRestrictionsOnThisValuesIfPossible(thisVarDescriptor: VariableDescriptor?) =
            thisVarDescriptor?.let {
                BooleanVariableValue.Undefined(
                        Restrictions.Specific.create(mapOf(it to setOf())),
                        Restrictions.Specific.create(mapOf(it to setOf()))
                )
            } ?: BooleanVariableValue.Undefined.WITH_NO_RESTRICTIONS

        override fun greaterOrEq(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue =
                !lessThan(other, thisVarDescriptor)

        override fun lessOrEq(
                other: IntegerVariableValue,
                thisVarDescriptor: VariableDescriptor?
        ): BooleanVariableValue =
                !greaterThan(other, thisVarDescriptor)

        private fun comparison(
                otherValue: Int,
                findIndex: (IntArray, Int) -> Int,
                createBoolean: (Set<Int>, Set<Int>) -> BooleanVariableValue
        ): BooleanVariableValue {
            val thisArray = this.values.toIntArray()
            thisArray.sort()
            val foundIndex = findIndex(thisArray, otherValue)
            val bound = if (foundIndex < 0) thisArray.size() else foundIndex
            val valuesWithLessIndices = thisArray.copyOfRange(0, bound).toSet()
            val valuesWithGreaterOrEqIndices = thisArray.copyOfRange(bound, thisArray.size()).toSet()
            return createBoolean(valuesWithLessIndices, valuesWithGreaterOrEqIndices)
        }
    }
}