package com.mafazaa.ainaa.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.UninstallRequest
import com.mafazaa.ainaa.domain.models.UninstallRequestStatus
import com.mafazaa.ainaa.ui.theme.red
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun UninstallRequestDialog(
    state: UninstallDialogState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit = {},
    onSubmitRequest: (reason: String) -> Unit,
    onStopProtectionPermanently: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.uninstall_request_tab_new),
        stringResource(R.string.uninstall_request_tab_list),
    )
    val isApproved = state.requests.any { it.status == UninstallRequestStatus.APPROVED }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(.93f)
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {

                // ── Header ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(red)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_text),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = stringResource(R.string.uninstall_request_dialog_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            tint = Color.White
                        )
                    }
                }

                // ── Tabs ─────────────────────────────────────────────────
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = red,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> NewRequestTab(
                        isLoading = state.isLoading,
                        onSubmit = onSubmitRequest,
                        isRequestSent = state.isRequestSent
                    )
                    1 -> RequestListTab(
                        requests = state.requests,
                        isLoading = state.isLoading,
                    )
                }

                if (isApproved) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStopProtectionPermanently,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = red
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.stop_protection_permanently),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Tab 0: Submit a new request ──────────────────────────────────────────────

@Composable
private fun NewRequestTab(
    isLoading: Boolean,
    onSubmit: (String) -> Unit,
    isRequestSent: Boolean = false
) {
    var reason by remember { mutableStateOf("") }
    var reasonError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.uninstall_request_new_hint),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = reason,
            onValueChange = {
                reason = it
                if (reasonError) reasonError = false
            },
            label = { Text(stringResource(R.string.uninstall_request_reason_label)) },
            placeholder = { Text(stringResource(R.string.uninstall_request_reason_placeholder)) },
            isError = reasonError,
            supportingText = if (reasonError) {
                { Text(stringResource(R.string.uninstall_request_reason_error)) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            maxLines = 5,
        )
        if (isRequestSent) {
            Text(stringResource(R.string.request_sent_successfully), color =  Color(0xFF2E7D32))
        }

        Button(
            onClick = {
                val trimmed = reason.trim()
                if (trimmed.length < 10) {
                    reasonError = true
                } else {
                    onSubmit(trimmed)
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = red),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )


            }else{
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
                Text(
                    text = stringResource(R.string.uninstall_request_send_button),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ── Tab 1: List of existing requests ─────────────────────────────────────────

@Composable
private fun RequestListTab(
    requests: List<UninstallRequest>,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = red)
            }
        } else if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.uninstall_request_empty),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests) { request ->
                    UninstallRequestItem(request = request)
                }
            }
        }
    }
}

// ── Single request card ───────────────────────────────────────────────────────

@Composable
private fun UninstallRequestItem(request: UninstallRequest) {
    val (statusColor, statusLabel) = when (request.status) {
        UninstallRequestStatus.APPROVED -> Pair(Color(0xFF2E7D32), stringResource(R.string.uninstall_status_approved))
        UninstallRequestStatus.REJECTED -> Pair(red, stringResource(R.string.uninstall_status_rejected))
        UninstallRequestStatus.PENDING  -> Pair(Color(0xFFE65100), stringResource(R.string.uninstall_status_pending))
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val instant = Instant.parse(request.timeCreated)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .withZone(ZoneId.systemDefault()) // Match user's local timezone

                val formattedDate = formatter.format(instant)
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                // Status chip
                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            // ID row with copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ID: ${request.id}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f, fill = false)
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(request.id))
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "Copy ID",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = request.reason,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}