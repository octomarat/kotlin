// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
fun bar(x: Int) = x + 1

fun foo(cond: Boolean) {
    val x: Int? = null

    if (x != null) {
        bar(x)
        if (<!SENSELESS_COMPARISON!>x != null<!>) {
            bar(x)
            if (cond) bar(x)
            if (cond) bar(x)
        }
        if (<!SENSELESS_COMPARISON!>x == null<!>) {
            bar(x)
        }
        if (<!SENSELESS_COMPARISON!>x == null<!>) bar(x) else bar(x)
        bar(bar(x))
    } else if (<!SENSELESS_COMPARISON!>x == null<!>) {
        bar(<!TYPE_MISMATCH!>x<!>)
        if (<!SENSELESS_COMPARISON!>x != null<!>) {
            bar(x)
            if (<!SENSELESS_COMPARISON!>x == null<!>) bar(x)
            if (<!SENSELESS_COMPARISON!>x == null<!>) bar(x) else bar(x)
            bar(bar(x) + bar(x))
        } else if (<!SENSELESS_COMPARISON!>x == null<!>) {
            bar(<!TYPE_MISMATCH!>x<!>)
        }
    }

}
