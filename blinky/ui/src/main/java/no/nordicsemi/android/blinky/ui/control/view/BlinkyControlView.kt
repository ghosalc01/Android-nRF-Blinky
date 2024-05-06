package no.nordicsemi.android.blinky.ui.control.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
internal fun BlinkyControlView(
    timeModeState: Boolean,
    buttonState: Boolean,
    dstState: Boolean,
    onStateChanged: (Boolean) -> Unit,
    ondstStateChanged:(Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        timeModeControlView(
            state = timeModeState,
            onStateChanged = onStateChanged,
        )
        DstControlView(
            state = dstState,
            ondstStateChanged = ondstStateChanged,
        )
    }
}

@Preview
@Composable
private fun BlinkyControlViewPreview() {
    NordicTheme {
        BlinkyControlView(
            timeModeState = true,
            buttonState = true,
            dstState = true,
            onStateChanged = {},
            ondstStateChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}