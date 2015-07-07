// FILE: KotlinFile.kt
fun foo(<!UNUSED_PARAMETER!>javaClass<!>: JavaClass) {
    javaClass.<!UNRESOLVED_REFERENCE!>something<!> = 1
}

// FILE: JavaClass.java
public class JavaClass {
    public void setSomething(int value) { }
}