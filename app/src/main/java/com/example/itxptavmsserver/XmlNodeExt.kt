package com.example.itxptavmsserver

import org.redundent.kotlin.xml.Node

fun Node.toTrimmedString(): String {
    return toString().replace("[\t\n]+".toRegex(), "")
}