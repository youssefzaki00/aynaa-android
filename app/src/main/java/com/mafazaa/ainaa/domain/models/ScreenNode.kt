package com.mafazaa.ainaa.domain.models

data class ScreenNode(
    /**
     * the type of the view (e.g., android.widget.Button, android.widget.TextView)
     */
    val cls: String?,
    val text: String?,
    val id: String?,
    /**
     * contentDescription
     */
    val desc: String?,
    val children: List<ScreenNode> = emptyList()
) {
    override fun toString(): String {//consider this as root
        return toString(0)
    }
    fun hasTextOrDescContaining(keyword: String): Boolean {
        if (text?.contains(keyword, true) == true || desc?.contains(keyword, true) == true) {
            return true
        }
        for (child in children) {
            if (child.hasTextOrDescContaining(keyword)) {
                return true
            }
        }
        return false
    }

    fun toString(level: Int): String {
        val indent = "  ".repeat(level)
        val sb = StringBuilder()
        sb.append("$indent- cls: $cls, text: $text, id: $id, desc: $desc\n")
        for (child in children) {
            sb.append(child.toString(level + 1))
        }
        return sb.toString()
    }
}