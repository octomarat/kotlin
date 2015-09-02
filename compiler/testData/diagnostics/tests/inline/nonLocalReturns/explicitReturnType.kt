fun inlineCallExplicitError(cond: Boolean): String {
    inlineFun lamba@ {
        if (cond) {
            return@lamba 2
        }
        1
    }

    return "x"
}

fun inlineCall(cond: Boolean): String {
    inlineFun lamba@ {
        if (cond) {
            return@lamba 2
        }
        1
    }

    return "x"
}

inline fun inlineFun(s: () -> Int) {
    s()
}


fun noInlineCall(cond: Boolean): String {
    noInline lambda@ {
        if (cond) {
            return@lambda 2
        }
        1
    }
    return "x"
}


fun noInline(s: () -> Int) {
    s()
}