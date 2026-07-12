package com.mafazaa.ainaa.domain.models

data class UninstallRequest (
    val reason:String,
    val id :String,
    val status: UninstallRequestStatus,
    val timeCreated:String
)
enum class UninstallRequestStatus{
    APPROVED,
    REJECTED,
    PENDING
}
