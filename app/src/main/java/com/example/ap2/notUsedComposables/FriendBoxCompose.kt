package com.example.ap2.notUsedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.R

@Composable
fun FriendBox(){
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .border(
            1.dp,
            Color.White
        )
        .background(Color.White)
        .padding(horizontal = 8.dp)
    ) {
        Text(
            "Freund 1",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterStart)
        )
        Icon(
            painter = painterResource(id = R.drawable.baseline_more_vert_24),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
                .clickable(onClick = { showMenu = true})
        )
    }
    if (showMenu) {
        FriendOptions(onDismiss = { showMenu = false })
    }
}

@Preview
@Composable
fun FriendBoxPreview() {
    FriendBox()
}