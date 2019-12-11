package projetift604.server

import io.ktor.network.tls.certificates.generateCertificate
import java.io.File

/**
 * dans l'eventualité d'utiliser https pour le cerveur, l'object créer un faux certificat ssl.
 */
object CertificateGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val jksFile = File("build/temporary.jks").apply {
            parentFile.mkdirs()
        }

        if (!jksFile.exists()) {
            generateCertificate(jksFile) // Generates the certificate
        }
    }
}