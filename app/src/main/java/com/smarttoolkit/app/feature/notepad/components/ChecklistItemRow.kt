package com.smarttoolkit.app.feature.notepad.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun ChecklistItemRow(
    text: String,
    isChecked: Boolean,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onEnterPressed: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isChecked) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "checklistTextColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.DragHandle,
            contentDescription = "Reorder",
            modifier = Modifier
                .size(20.dp)
                .padding(end = 4.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .padding(horizontal = 4.dp),
            textStyle = TextStyle(
                color = textColor,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onEnterPressed() }),
            singleLine = true
        )

        if (canDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Delete item",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
