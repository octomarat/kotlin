fun test(cond: Boolean) {
    @ann
    while (cond) {}

    @ann
    do {} while (2 < 1)

    @ann
    for (i in 1..2) {}
}

annotation class ann