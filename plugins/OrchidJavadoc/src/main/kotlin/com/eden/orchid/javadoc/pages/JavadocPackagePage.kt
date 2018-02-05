package com.eden.orchid.javadoc.pages

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.theme.pages.OrchidPage
import com.eden.orchid.javadoc.resources.PackageDocResource
import com.sun.javadoc.PackageDoc

class JavadocPackagePage(
        context: OrchidContext,
        val packageDoc: PackageDoc,
        val classes: List<JavadocClassPage>)
    : OrchidPage(PackageDocResource(context, packageDoc), "javadocPackage", packageDoc.name()) {

    val innerPackages: MutableList<JavadocPackagePage> = ArrayList()

    val summary: String
        get() {
            return context.compile("md", packageDoc.firstSentenceTags().joinToString(" ", transform = {it.text()}))
        }

    fun hasInterfaces(): Boolean {
        return classes.any { it.classDoc.isInterface }
    }

    val interfaces: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isInterface }
        }

    fun hasOrdinaryClasses(): Boolean {
        return classes.any { it.classDoc.isOrdinaryClass && !it.classDoc.isAbstract }
    }

    val ordinaryClasses: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isOrdinaryClass && !it.classDoc.isAbstract }
        }

    fun hasEnums(): Boolean {
        return classes.any { it.classDoc.isEnum }
    }

    val enums: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isEnum }
        }

    fun hasExceptions(): Boolean {
        return classes.any { it.classDoc.isException }
    }

    val exceptions: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isException }
        }

    fun hasErrors(): Boolean {
        return classes.any { it.classDoc.isError }
    }

    val errors: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isError }
        }

    fun hasAnnotations(): Boolean {
        return classes.any { it.classDoc.isAnnotationType }
    }

    val annotations: List<JavadocClassPage>
        get() {
            return classes.filter { it.classDoc.isAnnotationType }
        }

}
