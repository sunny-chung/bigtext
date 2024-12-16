# BigText Changelog

All notable changes to the BigText data structure library (DS) and its Jetpack Compose UI integration library (UI) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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

UI (not affecting Hello HTTP)
- If onTextLayout is null, layout would be incorrect
- If textTransformation is null, viewState.transformedText would never be set, leading to incorrect layout (always trimmed to single line)
- Forward deletes in BigMonospaceTextField did not update the UI immediately


## UI [1.0.0], DS [1.0.0] -- 2024-12-05

This version was bundled within Hello HTTP and has no standalone release.
