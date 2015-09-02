open class C {
}

fun C.foo() {}

open class X {
    companion object : C() {}
}

open class Y {
    companion object : C() {}
}

fun bar(cond: Boolean) {
    val x = X
    x.foo()
    X.foo()
    (X as C).foo()
    ((if (cond) X else Y) <!USELESS_CAST!>as C<!>).foo()
}
