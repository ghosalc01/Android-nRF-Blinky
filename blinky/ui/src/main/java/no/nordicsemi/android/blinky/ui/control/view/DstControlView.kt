package no.nordicsemi.android.blinky.ui.control.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.blinky.ui.R
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
internal fun DstControlView(
    state: Boolean,
    ondstStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .clickable { ondstStateChanged(!state) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = stringResource(R.string.blinky_dst_Mode),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.blinky_dst_descr),
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state, onCheckedChange = ondstStateChanged)
            }
        }
    }
}

@Composable
@Preview
private fun DstControlViewPreview() {
    NordicTheme {
        DstControlView(
            state = true,
            ondstStateChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}