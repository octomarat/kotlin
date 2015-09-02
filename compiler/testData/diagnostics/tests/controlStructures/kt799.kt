//KT-799 Allow 'return' expressions in conditionals assigned to variables

package kt799

fun test(cond: Boolean) {
    val <!UNUSED_VARIABLE!>a<!> : Int = if (cond) 6 else return // should be allowed

    val <!UNUSED_VARIABLE!>b<!> = if (cond) 6 else return // should be allowed

    doSmth(if (cond) 3 else return)

    doSmth(if (cond) 3 else return, <!TOO_MANY_ARGUMENTS!>1<!>)
}

val a : Nothing = <!RETURN_NOT_ALLOWED!>return 1<!>

val b = <!RETURN_NOT_ALLOWED!>return 1<!>

val c = doSmth(if ("".length() > 0) 3 else <!RETURN_NOT_ALLOWED!>return<!>)


fun f(<!UNUSED_PARAMETER!>mi<!>: Int = if ("".length() > 0) 0 else <!RETURN_NOT_ALLOWED!>return<!>) {}

fun doSmth(<!UNUSED_PARAMETER!>i<!>: Int) {
}
