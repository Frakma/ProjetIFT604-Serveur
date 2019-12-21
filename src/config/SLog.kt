package projetift604.config

import org.json.JSONException
import org.json.JSONObject


class SLog {
    companion object {
        private var indentLevel = 0
        private val stopDisplayAboveLevel = -1
        private val strCharLimit = 160

        enum class INDENT { PLUS, MINUS, SAME, RESET }

        private fun indentToTab(): String {
            return "\t".repeat(indentLevel) + "ᐅ "
        }

        fun log(str: String, indentOnce: Boolean = false, displayFull: Boolean = false) {
            var str_ = str
            if (indentOnce) {
                changeIndent(INDENT.PLUS)
            }
            if (stopDisplayAboveLevel != -1) {
                if (indentLevel > stopDisplayAboveLevel) {
                    if (indentOnce) {
                        changeIndent(INDENT.MINUS)
                    }
                    return
                }
            }
            if (!displayFull) {
                if (str.length > strCharLimit) {
                    str_ = str.substring(0, strCharLimit) + "..."
                }
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

fun getJSONObject(item: JSONObject, key: String): JSONObject? {
    try {
        return if (item.has(key) && !item.isNull(key)) {
            JSONObject(item.get(key))
        } else null
    } catch (e: JSONException) {
        System.err.println(e)
        return null
    }
}

fun getJSONString(item: JSONObject, key: String): String? {
    try {
        return if (item.has(key) && !item.isNull(key)) {
            item.get(key).toString()
        } else null
    } catch (e: JSONException) {
        System.err.println(e)
        return null
    }
}