package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

trait Trait {
    val t: Int
}

class TraitImpl : Trait {
    override val t: Int = 0
}

class TestDelegate : Trait by TraitImpl() {
    fun test() {
        testRenamed("try", { `try`@ while (false) {} })
    }
}

fun box(): String {
    TestDelegate().test()

    return "OK"
}