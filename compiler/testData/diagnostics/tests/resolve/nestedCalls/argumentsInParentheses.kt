interface Foo<T>

class Bar {
    fun <T> invoke(): Foo<T> = throw Exception()
}

class A {
    val bar = Bar()
}

fun fooInt(l: Foo<Int>) = l

fun test(bar: Bar, a: A, cond: Boolean) {
    // no elements with error types
    fooInt((bar()))
    fooInt(if (cond) bar() else bar())
    fooInt(label@ bar())
    fooInt(a.bar())
    fooInt(((label@ if (cond) (a.bar()) else bar())))
}