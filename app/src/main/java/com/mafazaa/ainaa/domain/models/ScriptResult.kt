package com.mafazaa.ainaa.domain.models

sealed class ScriptResult {
    data class Success(
        /**
         * script name for bowing which script was executed
         * e.g., "samsung accessibility "
         */
        val scriptName: String, val matched: Boolean) : ScriptResult()
    data class Error(val error: String) : ScriptResult()
}