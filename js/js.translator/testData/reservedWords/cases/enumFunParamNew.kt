package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

enum class Foo {
    BAR
    fun foo(new: String) {
    assertEquals("123", new)
    testRenamed("new", { new })
}

    fun test() {
        foo("123")
    }
}

fun box(): String {
    Foo.BAR.test()

    return "OK"
}