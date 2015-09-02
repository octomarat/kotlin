// !CHECK_TYPE
fun test(cond: Boolean) {
    val a = if (cond) {
        val x = 1
        ({ x })
    } else {
        { 2 }
    }
    a checkType {  _<() -> Int>() }
}