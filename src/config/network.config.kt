data class EventfulConfig(val DOMAIN: String, val APP_KEY: String)
data class FBConfig(val DOMAIN: String, val APP_ID: String, val APP_SECRET: String)
data class Config(val FB: FBConfig, val EVENTFUL: EventfulConfig) {
    companion object {
        val config: Config = Config(
            FBConfig(
                "https://graph.facebook.com/v5.0/",
                "454036578587951",
                "7b2be4392bf16a72e6ab1826b90ae438"
            ),
            EventfulConfig(
                "https://api.eventful.com/",
                "gnDwNrqpCQWw3P6J"
            )
        )

        fun get(): Config {
            return config
        }
    }
}

class SLog {
    companion object {
        private var indentLevel = 0

        enum class INDENT { PLUS, MINUS, SAME, RESET }

        private fun indentToTab(): String {
            return "\t".repeat(indentLevel) + "-"
        }

        fun log(str: String, indentOnce: Boolean = false) {
            if (indentOnce) {
                changeIndent(INDENT.PLUS)
            }
            System.out.println(indentToTab() + str)
            if (indentOnce) {
                changeIndent(INDENT.MINUS)
            }
        }

        fun changeIndent(level: INDENT? = INDENT.SAME) {
            when (level) {
                INDENT.PLUS -> indentLevel++
                INDENT.MINUS -> if (indentLevel > 0) {
                    indentLevel--
                }
                INDENT.RESET -> indentLevel = 0
                else -> indentLevel = indentLevel
            }
        }
    }
}