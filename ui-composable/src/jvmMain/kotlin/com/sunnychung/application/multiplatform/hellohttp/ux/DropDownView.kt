package com.sunnychung.application.multiplatform.hellohttp.ux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sunnychung.application.multiplatform.hellohttp.util.emptyToNull
import com.sunnychung.application.multiplatform.hellohttp.util.log
import com.sunnychung.application.multiplatform.hellohttp.ux.local.AppColor
import com.sunnychung.application.multiplatform.hellohttp.ux.local.LocalColor
import com.sunnychung.application.multiplatform.hellohttp.ux.local.LocalFont

@Composable
fun <T : DropDownable> ContextMenuView(
    isShowContextMenu: Boolean,
    onDismissRequest: () -> Unit,
    colors: AppColor,
    testTagParts: Array<Any?>?,
    populatedItems: List<T>,
    onClickItem: (T) -> Boolean,
    contentView: @Composable (RowScope.(it: T?, isLabel: Boolean, isSelected: Boolean, isClickable: Boolean) -> Unit)  = {it, isLabel, isSelected, isClickable ->
        AppText(
            text = it?.displayText.emptyToNull() ?: "--",
            color = if (!isLabel && isSelected) {
                colors.highlight
            } else if (isClickable) {
                colors.primary
            } else {
                colors.disabled
            },
            fontSize = LocalFont.current.contextMenuFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
                .padding(horizontal = 2.dp)
                .run {
                    if (isLabel && testTagParts != null) {
                        testTag(buildTestTag(*testTagParts, TestTagPart.DropdownLabel)!!.also { log.d { ">>> Dropdown Use TTag: $it" } })
                    } else {
                        this
                    }
                }
        )
    },
    selectedItem: T?,
    isClickable: Boolean
) {
    CursorDropdownMenu(
        expanded = isShowContextMenu,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.background(colors.backgroundContextMenu)
            .semantics {
                set(DropDownDisplayTexts, populatedItems.map { it.displayText })
            }
            .run {
                if (testTagParts != null) {
                    testTag(buildTestTag(*testTagParts, TestTagPart.DropdownMenu)!!)
                } else {
                    this
                }
            }
    ) {
        populatedItems.forEach { item ->
            if (item is DropDownDivider) {
                Column(modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .background(colors.line)
                    .fillMaxWidth()
                    .height(1.dp)
                ) {}
                return@forEach
            }

            Column(modifier = Modifier
                .clickable {
                    if (onClickItem(item)) {
                        onDismissRequest()
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
                .run {
                    if (testTagParts != null) {
                        testTag(buildTestTag(*testTagParts, TestTagPart.DropdownItem, item.displayText)!!)
                    } else {
                        this
                    }
                }
            ) {
                Row {
                    contentView(item, false, item.key == selectedItem?.key, isClickable && item.isEnabled)
                }
            }
        }
    }
}

interface DropDownable {
    val key: Any?
    val displayText: String
    val isEnabled: Boolean
        get() = true
}

data class DropDownValue(override val displayText: String) : DropDownable {
    override val key: String
        get() = displayText
}

data class DropDownKeyValue<T>(override val key: T, override val displayText: String, override val isEnabled: Boolean = true) : DropDownable

object DropDownDivider : DropDownable {
    override val key: Any?
        get() = "_divider"

    override val displayText: String
        get() = ""
}

data class DropDownMap<T>(private val values: List<DropDownKeyValue<T>>) {
    private val mapByKey = values.associateBy { it.key }

    val dropdownables = values

    operator fun get(key: T) = mapByKey[key]

}

val DropDownDisplayTexts = SemanticsPropertyKey<List<String>>(
    name = "DropDownDisplayTexts",
    mergePolicy = { parentValue, childValue ->
        parentValue ?: childValue
    }
)
