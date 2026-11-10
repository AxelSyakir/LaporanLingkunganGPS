// [file: MainTopBar.kt]
package com.example.gps.ui.theme.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gps.R
import com.example.gps.ui.theme.GPSTheme

@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    onNewsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.placeholdermaintopbar),
                contentDescription = "App logo",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNewsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.placeholderspeaker),
                    contentDescription = "Announcement",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.placeholdersettings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTopBarPreview() {
    GPSTheme {
        // Fixed: Call MainTopBar() instead of recursively calling MainTopBarPreview()
        MainTopBar()
    }
}