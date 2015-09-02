fun String?.foo(): String {
    return this ?: ""
}

class MyClass {
    fun bar(): String {
        var s: String? = null
        if ("".length() > 0)
            s = "42"
        return s.foo()
    }    
}