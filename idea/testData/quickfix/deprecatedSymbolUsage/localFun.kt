// "Replace with '1'" "true"

fun foo(): Int {
    @deprecated("", ReplaceWith("1"))
    fun localFun() = 1

    return <caret>localFun()
}