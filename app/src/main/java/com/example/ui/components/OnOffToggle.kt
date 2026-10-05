package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnOffToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "toggleContainer"
    )
    val selectedColor = MaterialTheme.colorScheme.primary
    val selectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 42.dp else 0.dp,
        label = "toggleThumb"
    )

    Surface(
        modifier = modifier
            .width(84.dp)
            .height(36.dp)
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch),
        shape = RoundedCornerShape(11.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            if (checked) selectedColor.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight().width(78.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ON",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (checked) selectedContent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (checked) selectedColor else MaterialTheme.colorScheme.outline)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (checked) "On" else "Off",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}