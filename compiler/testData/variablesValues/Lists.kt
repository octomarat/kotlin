fun basic() {
    val lst = listOf(1, 2)
    val mLst = arrayListOf(1, 2, 3)
    42
}

fun listWithTwoPossibleSizes(cond: Boolean) {
    val lst: List<Boolean>
    if (cond) {
        lst = listOf(false, false)
    }
    else {
        lst = arrayListOf(false, true, true)
    }
    42
}

fun listAdd() {
    val lst = arrayListOf(1, 2, 3)
    lst.add(4)
    lst.addAll(arrayOf(5, 6, 7, 8, 9, 10, 11))
    val lst2 = arrayListOf(1, 2)
    lst2.addAll(lst)
    42
}

fun listClear() {
    val lst = arrayListOf(1, 2, 3)
    lst.clear()
    42
}