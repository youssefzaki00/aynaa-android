package com.mafazaa.ainaa.ui.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.UninstallRequest
import com.mafazaa.ainaa.ui.common.TwoColorText
import com.mafazaa.ainaa.ui.theme.red

@Composable
fun SupportScreen(
    modifier: Modifier = Modifier,
    onSupportClick: () -> Unit = {},
    onJoinClick: () -> Unit = {},
    onShareLogFile: () -> Unit = {},
    onStopBlocking: () -> Unit = {},
    onOpenScreenShotWindow: () -> Unit = {},
    isBlocking: Boolean = false,
    onShowUninstallRequestDialog: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.support_us_label_text),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ActionCard(
            title = stringResource(R.string.donation_label_text),
            message = stringResource(R.string.donation_label_message),
            buttonText = stringResource(R.string.support_us_text),
            onButtonClick = onSupportClick,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ActionCard(
            title = stringResource(R.string.join_us_text),
            message = stringResource(R.string.join_us_message),
            buttonText = stringResource(R.string.join_text),
            onButtonClick = onJoinClick,

            )

        // ── Uninstall Request Card ────────────────────────────────────────
        Spacer(modifier = Modifier.height(4.dp))
        ActionCard(
            title = stringResource(R.string.uninstall_request_card_title),
            message = stringResource(R.string.uninstall_request_card_message),
            buttonText = stringResource(R.string.uninstall_request_button_text),
            onButtonClick = onShowUninstallRequestDialog,
            buttonContainerColor = red,
            buttonContentColor = Color.White
        )


        TwoColorText(
            black = stringResource(R.string.share_register_folder_text),
            red = stringResource(R.string.click_here_text)
        ) {
            onShareLogFile()
        }
        TwoColorText(
            black = stringResource(R.string.share_screen_not_blocked_text),
            red = stringResource(R.string.click_here_text)
        ) {
            onOpenScreenShotWindow()

        }
        if (BuildConfig.DEBUG) {//todo
            Spacer(modifier = Modifier.height(16.dp))
            val blockingText = if (isBlocking) {
                stringResource(R.string.stop_blocking)
            } else {
                stringResource(R.string.start_blocking)
            }
            TwoColorText(black = blockingText, red = stringResource(R.string.click_here_text)) {
                onStopBlocking()
            }
        }

    }
}

@Composable
fun ActionCard(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonContainerColor: Color = MaterialTheme.colorScheme.primary,
    buttonContentColor: Color = Color.White,
    elevation: Dp = 4.dp
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            ) {
                Text(text = buttonText)
            }
        }
    }
}
@Preview(showSystemUi = true)
@Composable
fun ActionCardPreview() {
    ActionCard(
        title = "Title",
        message = "This is a message",
        buttonText = "Button Text",
        onButtonClick = {}
    )

}