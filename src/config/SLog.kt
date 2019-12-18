package projetift604.config


class SLog {
    companion object {
        private var indentLevel = 0
        private val strCharLimit = 160

        enum class INDENT { PLUS, MINUS, SAME, RESET }

        private fun indentToTab(): String {
            return "\t".repeat(indentLevel) + "ᐅ "
        }

        fun log(str: String, indentOnce: Boolean = false) {
            var str_ = str
            if (indentOnce) {
                changeIndent(INDENT.PLUS)
            }
            if (str.length > strCharLimit) {
                str_ = str.substring(0, strCharLimit) + "..."
            }
            System.out.println(indentToTab() + str_)
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