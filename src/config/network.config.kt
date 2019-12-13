data class EventfulConfig(val DOMAIN: String, val APP_KEY: String)
data class FBConfig(val DOMAIN: String, val APP_ID: String, val APP_SECRET: String)
data class Config(val FB: FBConfig, val EVENTFUL: EventfulConfig) {
    companion object {
        private val config: Config = Config(
            FBConfig(
                "https://graph.facebook.com/v5.0/",
                "454036578587951",
                "7b2be4392bf16a72e6ab1826b90ae438"
            ),
            EventfulConfig(
                "https://eventful.com/rest",
                "gnDwNrqpCQWw3P6J"
            )
        )

        fun get(): Config {
            return config
        }
    }
}