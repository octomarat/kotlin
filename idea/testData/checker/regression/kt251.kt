class A() {
    var x: Int = 0
        get() = <error>"s"</error>
        set(value: <error>String</error>) {
            $x = <error>value</error>
        }
    val y: Int
        get(): <error>String</error> = "s"
    val z: Int
        get() {
            return <error>"s"</error>
        }

    var a: Any = 1
        set(v: <error>String</error>) {
            $a = v
        }
    val b: Int
        get(): <error>Any</error> = "s"
    val c: Int
        get() {
            return 1
        }
    val d = 1
        get() {
            return $d
        }
    val e = 1
        get(): <error>String</error> {
            return <error>$e</error>
        }

}