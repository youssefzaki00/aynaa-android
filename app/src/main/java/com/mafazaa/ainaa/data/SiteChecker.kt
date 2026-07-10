package com.mafazaa.ainaa.data

import com.mafazaa.ainaa.domain.models.ScreenAnalysis

data class SiteChecker(val pkg:String , val getUrl:(ScreenAnalysis)->String?)
val googleChecker = SiteChecker("com.android.chrome"){
    screenAnalysis ->
    screenAnalysis.root.children.getOrNull(1)?.children?.getOrNull(1)?.children?.getOrNull(1)?.text

}
val googleIncognitoChecker  = SiteChecker("com.android.chrome"){
        screenAnalysis ->
    screenAnalysis.root.children.getOrNull(1)?.children?.getOrNull(1)?.children?.getOrNull(1)?.text

}
val edgeChecker = SiteChecker("com.microsoft.emmx"){
    screenAnalysis ->
    screenAnalysis.root.children.getOrNull(2)?.children?.getOrNull(0)?.children?.getOrNull(0)?.children?.getOrNull(1)?.children?.getOrNull(1)?.text

}
val checkers = listOf(googleChecker,googleIncognitoChecker,edgeChecker)

fun getUrlFromBrowser(screenAnalysis: ScreenAnalysis) = checkers.filter { it.pkg==screenAnalysis.pkg }. firstNotNullOfOrNull { it.getUrl(screenAnalysis) }