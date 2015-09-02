package <!SYNTAX!>return<!>

class A {
    fun outer(cond: Boolean) {
        fun inner() {
            if (cond)
                return@inner
            else
                <!RETURN_NOT_ALLOWED!>return@outer<!>
        }
        if (cond)
            <!NOT_A_RETURN_LABEL!>return@A<!>
        else if (cond)
            return<!UNRESOLVED_REFERENCE!>@inner<!>
        return@outer
    }
}
