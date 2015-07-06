target(AnnotationTarget.CLASSIFIER)
annotation class base

target(AnnotationTarget.ANNOTATION_CLASS)
annotation class meta

base class Outer {
    base <!WRONG_ANNOTATION_TARGET!>meta<!> class Nested

    <!WRONG_ANNOTATION_TARGET!>base<!> meta annotation class Annotated

    fun foo() {
        @base <!WRONG_ANNOTATION_TARGET!>@meta<!> class Local
    }
}