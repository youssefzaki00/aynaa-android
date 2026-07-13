package com.mafazaa.ainaa.ui.protection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R.drawable.shield_with_heart
import com.mafazaa.ainaa.R.string.activate_protection
import com.mafazaa.ainaa.R.string.feature_apps
import com.mafazaa.ainaa.R.string.feature_gambling
import com.mafazaa.ainaa.R.string.feature_network
import com.mafazaa.ainaa.R.string.feature_porn
import com.mafazaa.ainaa.R.string.i_agree_to_the
import com.mafazaa.ainaa.R.string.privacy_policy
import com.mafazaa.ainaa.R.string.protection_description
import com.mafazaa.ainaa.R.string.protection_title
import com.mafazaa.ainaa.R.string.terms_of_service
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.ui.common.TwoColorText
import com.mafazaa.ainaa.ui.theme.red
import com.mafazaa.ainaa.utils.openUrl

@Composable
fun EnableProtectionScreen(
    modifier: Modifier = Modifier,
    report: () -> Unit,
    enableProtection: (DnsProtectionLevel) -> Unit,
    selectedLevel: DnsProtectionLevel,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // optional spacing between the two
    ) {
        var selectedLevel by remember { mutableStateOf(selectedLevel) }
        val context = LocalContext.current
        var termsAccepted by rememberSaveable { mutableStateOf(false) }
        var privacyAccepted by rememberSaveable { mutableStateOf(false) }
        val features: List<String> = listOf(
            stringResource(feature_porn),
            stringResource(feature_gambling),
            stringResource(feature_apps),
            stringResource(feature_network)
        )
        // The source design is RTL (Arabic), so we force RTL layout direction
        // for this composable regardless of the app's locale.
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, red),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title row: shield icon + title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(protection_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.size(8.dp))
                    Icon(
                        painter = painterResource(shield_with_heart),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Description
                Text(
                    text = stringResource(protection_description),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(20.dp))

                // Feature checklist
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    features.forEach { feature ->
                        FeatureRow(feature)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Activate button
                Button(
                    onClick = {
                        enableProtection(selectedLevel)
                    },
                    enabled = termsAccepted && privacyAccepted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color.LightGray,
                        containerColor = red,
                        contentColor = Color.White
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(activate_protection),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AgreementRow(
                    text = stringResource(i_agree_to_the),
                    linkText = stringResource(terms_of_service),
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    onLinkClick = { context.openUrl("https://aynaa.org/terms") }
                )

                AgreementRow(
                    text = stringResource(i_agree_to_the),
                    linkText = stringResource(privacy_policy),
                    checked = privacyAccepted,
                    onCheckedChange = { privacyAccepted = it },
                    onLinkClick = { context.openUrl("https://aynaa.org/privacy") }
                )
            }


        }
    }

}

@Composable
fun FeatureRow(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = red,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun AgreementRow(
    text: String,
    linkText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = red)
        )
        TwoColorText(
            black = text,
            red = linkText,
            onClick = onLinkClick
        )
    }
}


