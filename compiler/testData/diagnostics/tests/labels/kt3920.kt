// !DIAGNOSTICS: -UNUSED_VARIABLE
//KT-3920 Labeling information is lost when passing through some expressions

fun test(cond: Boolean) {
    run f@{
        val x = if (cond) return@f 1 else 2
        2
    }
}

fun <T> run(f: () -> T): T = f()