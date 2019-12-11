package projetift604.server

import io.ktor.network.tls.certificates.generateCertificate
import org.apache.commons.codec.binary.Hex
import java.io.File
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

@Throws(Exception::class)
fun generateHashWithHmac256(message: String, key: String): String {
    val sha256_HMAC = Mac.getInstance("HmacSHA256")
    val secret_key = SecretKeySpec(key.toByteArray(charset("UTF-8")), "HmacSHA256")
    sha256_HMAC.init(secret_key)
    return Hex.encodeHexString(sha256_HMAC.doFinal(message.toByteArray(charset("UTF-8"))))
}