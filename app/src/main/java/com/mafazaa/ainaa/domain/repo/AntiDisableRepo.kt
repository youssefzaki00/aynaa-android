package com.mafazaa.ainaa.domain.repo

import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScriptCode
import com.mafazaa.ainaa.domain.models.ScriptResult

interface AntiDisableRepo {
    fun setCodes(codes: List<ScriptCode>)
    fun evaluate(screenAnalysis: ScreenAnalysis): ScriptResult
}

