package com.example.ap2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SmallMarker(onExpandRequested: () -> Unit) {
    //State Remember für SneakPeekMarker, ob dieser angezeigt wird oder nicht
    var isSneakPeekVisible by remember { mutableStateOf(false) }

    //box damit der Marker in der Mitte ist
    Box() {
        //damit der Button nur das Icon ist
        Icon(
            //woher das Icon kommt
            painter = painterResource(R.drawable.baseline_place_24),
            contentDescription = null,
            //Farbänderung des Icons
            tint = Color.Red,
            //Vergrößerung des Icons und macht das Icon clickable
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    //damit kein graußes Quadrat im Hintergrund ist
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                    // zeigt den SneakPeekMarker an
                ) { isSneakPeekVisible = true },
        )

        if (isSneakPeekVisible) {
            SneakPeekMarker(
                onDismiss = { isSneakPeekVisible = false },
                onExpand = {
                    onExpandRequested()
                    isSneakPeekVisible = false // Close small when opening big
                }
            )
        }
    }
}

@Preview
@Composable
fun SmallMarkerPreview() {
    SmallMarker(onExpandRequested = {})
}