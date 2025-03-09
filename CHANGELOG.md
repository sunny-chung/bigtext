# BigText Changelog

All notable changes to the BigText data structure library (DS) and its Jetpack Compose UI integration library (UI) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

Nothing yet.


## UI [2.1.0] - 2025-03-09

Breaking change is introduced in this version to reduce rendering latency of multiple small general-purpose text fields.

### Removed
UI
- `provideUiCoroutineContext` -- its use case is supported by `onHeavyComputation` now.

### Change
UI
- `onHeavyComputation` is not suspend function anymore, as suspend functions must introduce unpredictable latency


## UI [2.0.10] - 2025-03-01

### Fixed
UI
- `clearAllBigTextWorkerCoroutineContexts()` was not cleaning properly


## UI [2.0.4] - 2025-02-25

### Fixed
UI
- Most global functions are unresolvable due to a Kotlin compiler bug producing a corrupted kotlin_module file (KT-46038)


## UI [2.0.3] - 2025-02-24

### New
UI
- `clearAllBigTextWorkerCoroutineContexts()` for cleaning the environment of failed tests


## UI [2.0.2] , DS [2.0.1] - 2025-02-23

### New
UI
- `provideUiCoroutineContext` -- an experimental API to improve the concurrent UI rendering latency

### Change
DS
- `weakRefOf()` and `WeakRefKey()` now accept a nullable parameter

### Fixed
UI
- Text input via IME was broken
- Double clicking in text field may lose focus
- Scroll deltas are consumed even the text or text field is not scrollable
- Colored text background among characters are not continuous


## UI [2.0.1] - 2025-01-25

### New
UI
- Dragging to scroll
- `PasswordIncrementalTransformation` -- an equivalent to `androidx.compose.ui.text.input.PasswordVisualTransformation`
- Experimental API `CoreBigTextField`, which is the underlying implementation of `BigTextField` and `BigTextLabel` with more experimental parameters
- `onFinishInit` -- an experimental API as a workaround to request the focus initially

### Change
UI
- If the text field is not set to fill max size, now it wraps by content size with a minimum size of 100 dp width and one line height.

### Fixed
UI
- `onTextChange` was often not called when there is a text change
- Crash when `BigTextFieldState.replaceTextAtCursor` is called with a selection.
- Incorrect selection behavior if the "Shift" key was holding while the focus was going away
- For BigTextLabel, soft wrap should be on by default
- Setting an initial selection range has no effect
- Setting an initial cursor position would crash

## UI [2.0.0], DS [2.0.0] - 2025-01-05

### Breaking Change
UI
- BigMonospaceTextField is renamed to BigTextField, BigMonospaceText is renamed to BigTextLabel 
- BigTextManipulator and onTextManipulatorReady are removed. Replaced by BigText, BigTextFieldState and BigTextViewState.
- Default cursor color has been changed

### New
Common
- Option to turn off soft wrap

UI
- Support non-monospace fonts
- Single line input option
- Max input length

### Changed
UI
- Cursor twinkling behavior

### Fixed
UI
- Pressing left or right keys without holding [Shift] should cancel selection unconditionally
- Non-editable texts are still editable in some way

UI (not affecting Hello HTTP)
- If onTextLayout is null, layout would be incorrect
- If textTransformation is null, viewState.transformedText would never be set, leading to incorrect layout (always trimmed to single line)
- Forward deletes in BigMonospaceTextField did not update the UI immediately
- Non-selectable texts are selectable, and have an incorrect pointer icon


## UI [1.0.0], DS [1.0.0] -- 2024-12-05

This version was bundled within Hello HTTP and has no standalone release.
