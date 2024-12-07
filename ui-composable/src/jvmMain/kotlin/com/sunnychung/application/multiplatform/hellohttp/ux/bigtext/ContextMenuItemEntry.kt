package com.sunnychung.application.multiplatform.hellohttp.ux.bigtext

open class ContextMenuItemEntry(val type: Type, val displayText: String, val isEnabled: Boolean, val testTag: String, val action: () -> Unit) {
    enum class Type {
        Button, Divider
    }
}

class ContextMenuItemButton(displayText: String, isEnabled: Boolean, testTag: String, action: () -> Unit) :
    ContextMenuItemEntry(type = Type.Button, displayText = displayText, isEnabled = isEnabled, testTag = testTag, action = action)

class ContextMenuItemDivider : ContextMenuItemEntry(type = Type.Divider, displayText = "", isEnabled = false, testTag = "", action = {})
