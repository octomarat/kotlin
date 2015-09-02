fun inlineCall(cond: Boolean): String {
    inlineFun {
        if (cond) {
            return@inlineCall ""
        }
        1
    }

    return "x"
}

inline fun inlineFun(s: ()->Int) {
    s()
}


fun noInlineCall(cond: Boolean): String {
    noInline {
        if (cond) {
            <!RETURN_NOT_ALLOWED!>return@noInlineCall ""<!>
        }
        1
    }

    return "x"
}


fun noInline(s: ()->Int) {
    s()
}