package com.gmail.uwriegel.superfit.extensions

import org.xmlpull.v1.XmlSerializer

/**
 * Created by urieg on 03.01.2018.
 */
fun XmlSerializer.document(encoding: String, standAlone: Boolean, init: XmlSerializer.() -> Unit): XmlSerializer {
    this.startDocument(encoding, standAlone)
    this.init()
    this.endDocument()
    this.flush()
    return this
}

fun XmlSerializer.element(namespace: String?, name: String, init: XmlSerializer.() -> Unit): XmlSerializer {
    this.startTag(namespace, name)
    this.init()
    this.endTag(namespace, name)
    return this
}

fun XmlSerializer.element(namespace: String?, name: String, content: String) {
    this.startTag(namespace, name)
    this.text(content)
    this.endTag(namespace, name)
}


