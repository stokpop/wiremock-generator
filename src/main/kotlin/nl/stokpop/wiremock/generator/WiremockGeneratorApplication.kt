package nl.stokpop.wiremock.generator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WiremockGeneratorApplication

fun main(args: Array<String>) {
    runApplication<WiremockGeneratorApplication>(*args)
}
