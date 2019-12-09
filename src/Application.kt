package projetift604

import io.ktor.application.Application
import serveur.ServeurREST

//fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun main(args: Array<String>): Unit = ServeurREST().start()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

}

