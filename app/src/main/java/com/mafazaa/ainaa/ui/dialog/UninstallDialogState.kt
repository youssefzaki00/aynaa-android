package com.mafazaa.ainaa.ui.dialog

import com.mafazaa.ainaa.domain.models.UninstallRequest

data class UninstallDialogState(
    val requests: List<UninstallRequest> = emptyList(),
    val isLoading: Boolean = false,
    val isRequestSent: Boolean = false,
)
