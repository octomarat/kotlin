// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_EXPRESSION

import java.util.HashSet

fun test123(cond: Boolean) {
    val g: (Int) -> Unit = if (cond) {
        val set = HashSet<Int>();
        { i ->
            set.add(i)
        }
    }
    else {
        { it -> it }
    }
}