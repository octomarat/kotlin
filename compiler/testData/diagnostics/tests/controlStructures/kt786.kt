package kt786

//KT-786 Exception on incomplete code with 'when'
fun foo() : Int {
    val d = 2
    var z = 0
    when(d) {
        5, 3 -> <!UNUSED_CHANGED_VALUE, UNREACHABLE_CODE!>z++<!>
        <!ELSE_MISPLACED_IN_WHEN!>else<!> -> { <!UNUSED_VALUE!>z =<!> -1000 }
        <!UNREACHABLE_CODE!>return z -> 34<!>
    }
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

//test unreachable code
fun fff(): Int {
    var d = 3
    when(d) {
        4 -> <!UNUSED_EXPRESSION, UNREACHABLE_CODE!>21<!>
        return 2 -> <!UNREACHABLE_CODE!>return 47<!>
        <!UNREACHABLE_CODE!>bar() -> 45<!>
        <!UNREACHABLE_CODE!>444 -> true<!>
    }
    <!UNREACHABLE_CODE!>return 34<!>
}

fun bar(): Int = 8