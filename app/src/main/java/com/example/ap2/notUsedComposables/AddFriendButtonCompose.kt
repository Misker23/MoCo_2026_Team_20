package com.example.ap2.notUsedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ap2.R

@Composable
fun AddFriendButton(
    onClick: () -> Unit // Parameter hinzugefügt
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.LightGray.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick) // Das übergebene Lambda wird hier aufgerufen
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add Friend")
        }
    }
}

@Preview
@Composable
fun AddFriendButtonPreview() {
    // Ein leeres Lambda {} für den onClick-Parameter
    AddFriendButton(onClick = {})
}