fun foo(<!UNRESOLVED_REFERENCE!>varargs<!> <!UNUSED_PARAMETER!>f<!> : Int) {}

var bar : Int = 1
  set(<!UNRESOLVED_REFERENCE!>varargs<!> v) {}

val x : (Int) -> Int = {<!UNRESOLVED_REFERENCE, WRONG_ANNOTATION_TARGET!>@varargs<!> <!TYPE_MISMATCH!>x<!> <!DEPRECATED_STATIC_ASSERT!>: Int<!> <!SYNTAX!>-> x<!>}

class Hello(<!UNRESOLVED_REFERENCE!>varargs<!> <!UNUSED_PARAMETER!>args<!>: Any) {
}