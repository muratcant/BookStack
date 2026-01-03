package org.muratcant.bookstack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookStackApplication

fun main(args: Array<String>) {
    runApplication<BookStackApplication>(*args)
}
