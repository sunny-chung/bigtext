@file:OptIn(ExperimentalBigTextUiApi::class)

package com.sunnychung.lib.multiplatform.bigtext.ux

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.insertTextAtCursor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.input.CommitTextCommand
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.SetComposingTextCommand
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextInputSession
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Severity
import com.sunnychung.lib.multiplatform.bigtext.annotation.ExperimentalBigTextUiApi
import com.sunnychung.lib.multiplatform.bigtext.compose.ComposeUnicodeCharMeasurer
import com.sunnychung.lib.multiplatform.bigtext.core.BigText
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeCallback
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEvent
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextChangeEventType
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextDecorator
import com.sunnychung.lib.multiplatform.bigtext.core.BigTextImpl
import com.sunnychung.lib.multiplatform.bigtext.core.layout.MonospaceTextLayouter
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.core.transform.BigTextTransformerImpl
import com.sunnychung.lib.multiplatform.bigtext.core.transform.ConcurrentBigTextTransformed
import com.sunnychung.lib.multiplatform.bigtext.core.transform.IncrementalTextTransformation
import com.sunnychung.lib.multiplatform.bigtext.extension.contains
import com.sunnychung.lib.multiplatform.bigtext.extension.intersect
import com.sunnychung.lib.multiplatform.bigtext.extension.isCtrlOrCmdPressed
import com.sunnychung.lib.multiplatform.bigtext.extension.replaceAll
import com.sunnychung.lib.multiplatform.bigtext.extension.runIf
import com.sunnychung.lib.multiplatform.bigtext.extension.toTextInput
import com.sunnychung.lib.multiplatform.bigtext.platform.MacOS
import com.sunnychung.lib.multiplatform.bigtext.platform.currentOS
import com.sunnychung.lib.multiplatform.bigtext.util.AnnotatedStringBuilder
import com.sunnychung.lib.multiplatform.bigtext.util.annotatedString
import com.sunnychung.lib.multiplatform.bigtext.util.buildTestTag
import com.sunnychung.lib.multiplatform.bigtext.util.debouncedStateOf
import com.sunnychung.lib.multiplatform.bigtext.util.isSurrogatePairFirst
import com.sunnychung.lib.multiplatform.bigtext.util.string
import com.sunnychung.lib.multiplatform.bigtext.util.weakRefOf
import com.sunnychung.lib.multiplatform.bigtext.ux.compose.rememberLast
import com.sunnychung.lib.multiplatform.kdatetime.KInstant
import com.sunnychung.lib.multiplatform.kdatetime.extension.milliseconds
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

private val NEW_LINE_REGEX = "\r?\n".toRegex()

@Composable
fun BigTextLabel(
    modifier: Modifier = Modifier,
    text: BigText,
    padding: PaddingValues = PaddingValues(4.dp),
    fontSize: TextUnit = LocalTextStyle.current.fontSize,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = LocalTextStyle.current.color,
    contextMenu: @Composable (isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String) -> Unit =
        { isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String ->
            DefaultBigTextFieldContextMenu(isVisible = isVisible, onDismiss = onDismiss, entries = entries, testTag = testTag)
        },
    isSelectable: Boolean = false,
    isSoftWrapEnabled: Boolean = true,
    inputFilter: BigTextInputFilter? = null,
    textTransformation: IncrementalTextTransformation<*>? = null,
    textDecorator: BigTextDecorator? = null,
    scrollState: ScrollState = rememberScrollState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    viewState: BigTextViewState = remember { BigTextViewState() },
    onPointerEvent: ((event: PointerEvent, tag: String?) -> Unit)? = null,
    onTextLayout: ((BigTextSimpleLayoutResult) -> Unit)? = null,
    onTransformInit: ((BigTextTransformed) -> Unit)? = null,
) = CoreBigTextField(
    modifier = modifier,
    text = text,
    padding = padding,
    fontSize = fontSize,
    fontFamily = fontFamily,
    color = color,
    contextMenu = contextMenu,
    isSelectable = isSelectable,
    isEditable = false,
    isSoftWrapEnabled = isSoftWrapEnabled,
    onTextChange = {},
    inputFilter = inputFilter,
    textTransformation = textTransformation,
    textDecorator = textDecorator,
    scrollState = scrollState,
    horizontalScrollState = horizontalScrollState,
    viewState = viewState,
    onPointerEvent = onPointerEvent,
    onTextLayout = onTextLayout,
    onTransformInit = onTransformInit,
)

@Composable
fun BigTextField(
    modifier: Modifier = Modifier,
    textFieldState: BigTextFieldState,
    padding: PaddingValues = PaddingValues(4.dp),
    fontSize: TextUnit = LocalTextStyle.current.fontSize,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = LocalTextStyle.current.color,
    cursorColor: Color = LocalTextStyle.current.color,
    contextMenu: @Composable (isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String) -> Unit =
        { isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String ->
            DefaultBigTextFieldContextMenu(isVisible = isVisible, onDismiss = onDismiss, entries = entries, testTag = testTag)
        },
    onTextChange: (BigTextChangeEvent) -> Unit = {},
    isSingleLineInput: Boolean = false,
    isSoftWrapEnabled: Boolean = !isSingleLineInput,
    maxInputLength: Long = Long.MAX_VALUE,
    inputFilter: BigTextInputFilter? = null,
    textTransformation: IncrementalTextTransformation<*>? = null,
    textDecorator: BigTextDecorator? = null,
    scrollState: ScrollState = rememberScrollState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    keyboardInputProcessor: BigTextKeyboardInputProcessor? = null,
    onPointerEvent: ((event: PointerEvent, tag: String?) -> Unit)? = null,
    onTextLayout: ((BigTextSimpleLayoutResult) -> Unit)? = null,
    onHeavyComputation: suspend (computation: suspend () -> Unit) -> Unit = { it() },
) {
    BigTextField(
        modifier = modifier,
        text = textFieldState.text,
        padding = padding,
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = color,
        cursorColor = cursorColor,
        isSoftWrapEnabled = isSoftWrapEnabled,
        contextMenu = contextMenu,
        onTextChange = {
            onTextChange(it)
            textFieldState.emitValueChange(it.changeId)
        },
        isSingleLineInput = isSingleLineInput,
        maxInputLength = maxInputLength,
        inputFilter = inputFilter,
        textTransformation = textTransformation,
        textDecorator = textDecorator,
        scrollState = scrollState,
        horizontalScrollState = horizontalScrollState,
        viewState = textFieldState.viewState,
        keyboardInputProcessor = keyboardInputProcessor,
        onPointerEvent = onPointerEvent,
        onTextLayout = onTextLayout,
        onHeavyComputation = onHeavyComputation,
    )
}

@Composable
fun BigTextField(
    modifier: Modifier = Modifier,
    text: BigText,
    padding: PaddingValues = PaddingValues(4.dp),
    fontSize: TextUnit = LocalTextStyle.current.fontSize,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = LocalTextStyle.current.color,
    cursorColor: Color = LocalTextStyle.current.color,
    contextMenu: @Composable (isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String) -> Unit =
        { isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String ->
            DefaultBigTextFieldContextMenu(isVisible = isVisible, onDismiss = onDismiss, entries = entries, testTag = testTag)
        },
    onTextChange: (BigTextChangeEvent) -> Unit,
    isSingleLineInput: Boolean = false,
    isSoftWrapEnabled: Boolean = !isSingleLineInput,
    maxInputLength: Long = Long.MAX_VALUE,
    inputFilter: BigTextInputFilter? = null,
    textTransformation: IncrementalTextTransformation<*>? = null,
    textDecorator: BigTextDecorator? = null,
    scrollState: ScrollState = rememberScrollState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    viewState: BigTextViewState = remember(weakRefOf(text)) { BigTextViewState() },
    keyboardInputProcessor: BigTextKeyboardInputProcessor? = null,
    onPointerEvent: ((event: PointerEvent, tag: String?) -> Unit)? = null,
    onTextLayout: ((BigTextSimpleLayoutResult) -> Unit)? = null,
    onHeavyComputation: suspend (computation: suspend () -> Unit) -> Unit = { it() },
) = CoreBigTextField(
    modifier = modifier,
    text = text,
    padding = padding,
    fontSize = fontSize,
    fontFamily = fontFamily,
    color = color,
    cursorColor = cursorColor,
    isSoftWrapEnabled = isSoftWrapEnabled,
    contextMenu = contextMenu,
    isSelectable = true,
    isEditable = true,
    onTextChange = onTextChange,
    isSingleLineInput = isSingleLineInput,
    maxInputLength = maxInputLength,
    inputFilter = inputFilter,
    textTransformation = textTransformation,
    textDecorator = textDecorator,
    scrollState = scrollState,
    horizontalScrollState = horizontalScrollState,
    viewState = viewState,
    keyboardInputProcessor = keyboardInputProcessor,
    onPointerEvent = onPointerEvent,
    onTextLayout = onTextLayout,
    onHeavyComputation = onHeavyComputation,
)

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class,
    DelicateCoroutinesApi::class,
)
@ExperimentalBigTextUiApi
@Composable
fun CoreBigTextField(
    modifier: Modifier = Modifier,
    text: BigText,
    padding: PaddingValues = PaddingValues(4.dp),
    fontSize: TextUnit = LocalTextStyle.current.fontSize,
    fontFamily: FontFamily = FontFamily.Monospace,
    color: Color = LocalTextStyle.current.color,
    cursorColor: Color = LocalTextStyle.current.color,
    isSelectable: Boolean = false,
    isEditable: Boolean = false,
    isSoftWrapEnabled: Boolean = false,
    contextMenu: @Composable (isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String) -> Unit =
        { isVisible: Boolean, onDismiss: () -> Unit, entries: List<ContextMenuItemEntry>, testTag: String ->
            DefaultBigTextFieldContextMenu(isVisible = isVisible, onDismiss = onDismiss, entries = entries, testTag = testTag)
        },
    onTextChange: (BigTextChangeEvent) -> Unit,
    isSingleLineInput: Boolean = false,
    maxInputLength: Long = Long.MAX_VALUE,
    inputFilter: BigTextInputFilter? = null,
    textTransformation: IncrementalTextTransformation<*>? = null,
    textDecorator: BigTextDecorator? = null,
    scrollState: ScrollState = rememberScrollState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    viewState: BigTextViewState = remember(weakRefOf(text)) { BigTextViewState() },
//    interactionSource: MutableInteractionSource = remember(weakRefOf(text)) { MutableInteractionSource() },
    keyboardInputProcessor: BigTextKeyboardInputProcessor? = null,
    onPointerEvent: ((event: PointerEvent, tag: String?) -> Unit)? = null,
    onTextLayout: ((BigTextSimpleLayoutResult) -> Unit)? = null,
    onHeavyComputation: suspend (computation: suspend () -> Unit) -> Unit = { it() },
    onTransformInit: ((BigTextTransformed) -> Unit)? = null,
    onFinishInit: () -> Unit = {},
    provideUiCoroutineContext: () -> CoroutineContext = { EmptyCoroutineContext },
) {
    log.d { "CoreBigMonospaceText recompose" }

    val density = LocalDensity.current
    val textSelectionColors = LocalTextSelectionColors.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textInputService = LocalTextInputService.current

    val textStyle = LocalTextStyle.current.copy(
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = color,
        letterSpacing = 0.sp,
        fontSynthesis = FontSynthesis.None,
    )

    val coroutineScope = rememberCoroutineScope(provideUiCoroutineContext)
    val heavyJobScope = rememberCoroutineScope {
        newFixedThreadPoolContext(2, "BigTextFieldHeavyCoroutines")
    }
    val focusRequester = remember { FocusRequester() }
    val textMeasurer = rememberTextMeasurer(0)
    var lineHeight by remember { mutableStateOf(0f) }
    val textLayouter = remember(density, fontFamilyResolver, textStyle, textMeasurer) {
        log.d { "Recreate layouter" }
        MonospaceTextLayouter(
            ComposeUnicodeCharMeasurer(
//                measurer = TextMeasurer(
//                    fontFamilyResolver,
//                    density,
//                    LayoutDirection.Ltr,
//                    0,
//                ),
                measurer = textMeasurer,
                style = textStyle,
//                density, fontFamilyResolver
            )
        ).also {
            lineHeight = (it.charMeasurer as ComposeUnicodeCharMeasurer).getRowHeight()
        }
    }
    // if the value of `viewState.isLayoutDisabled` is changed, trigger a recomposition
    viewState.isLayoutDisabledFlow.collectAsState(initial = false).value
    val isLayoutEnabled = !viewState.isLayoutDisabled // not using the value from flow because it is not instantly updated

    var textInputSessionRef by remember { mutableStateOf<TextInputSession?>(null) }
    val textInputSessionUpdatedRef by rememberUpdatedState(weakRefOf(textInputSessionRef))

    var isCursorVisible by remember { mutableStateOf(true) }
    val cursorShowTrigger = remember { Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST) }

    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val (contentWidth, isContentWidthLatest) = debouncedStateOf(200.milliseconds(), tolerateCount = 1, text) { /* handle the first non-zero width instantly */
        width - with(density) {
            (padding.calculateStartPadding(LayoutDirection.Ltr) + padding.calculateEndPadding(LayoutDirection.Ltr)).toPx()
        }
    }
    var layoutResult by remember(textLayouter, width) { mutableStateOf<BigTextSimpleLayoutResult?>(null) }
    var numOfComputationsInProgress by remember { mutableStateOf(0) }
    var isTransformedStateReady by remember(weakRefOf(text), textTransformation) {
        mutableStateOf(false)
    }
    // isComponentReady is a function, because its dependent variable can change within a recomposition
    val isComponentReady = fun(): Boolean {
        return numOfComputationsInProgress <= 0 && isTransformedStateReady && contentWidth > 0
    }
    val onFinishInitRef by rememberUpdatedState(weakRefOf(onFinishInit))
    var hasCalledFinishInit by remember(weakRefOf(text)) { mutableStateOf(false) }
    var forceRecompose by remember { mutableStateOf(0L) }
    forceRecompose

    viewState.version // observe value changes

    fun callFinishInitIfReady() {
        if (isComponentReady() && !hasCalledFinishInit) {
            coroutineScope.launch { // defer execution to make "requestFocus" working
                if (isComponentReady() && !hasCalledFinishInit) {
                    log.d { "Calling finish init" }
                    onFinishInitRef.get()?.invoke()
                    hasCalledFinishInit = true
                }
            }
        }
    }

    callFinishInitIfReady()

    fun heavyCompute(computation: suspend () -> Unit) {
        ++numOfComputationsInProgress
        ++viewState.numOfComputationsInProgress
        heavyJobScope.launch {
            onHeavyComputation(computation)
            coroutineScope.launch {
                --numOfComputationsInProgress
                --viewState.numOfComputationsInProgress
            }
        }
    }

    val transformedText: BigTextTransformed = remember(weakRefOf(text), textTransformation) {
        log.d { "CoreBigMonospaceText recreate BigTextTransformed $text $textTransformation" }
        BigTextTransformerImpl(
            text,
            charSequenceBuilderFactory = { AnnotatedStringBuilder(it) },
            charSequenceFactory = { (it as AnnotatedStringBuilder).toAnnotatedString() },
        )
            .let {
                if (text.isThreadSafe) {
                    ConcurrentBigTextTransformed(it)
                } else {
                    it
                }
            }
            .also {
//                log.d { "transformedText = |${it.buildString()}|" }
                if (log.config.minSeverity <= Severity.Verbose) {
                    it.printDebug("transformedText")
                }
            }
    }
    if (numOfComputationsInProgress <= 0) {
        transformedText.decorator = textDecorator
//        transformedText.setSoftWrapEnabled(isSoftWrapEnabled)
    }

    val textRef = weakRefOf(text)
    val transformedTextRef = weakRefOf(transformedText)
//    val onTextChangeRef = weakRefOf(onTextChange)
//    val onTextChangeUpdated = rememberUpdatedState(onTextChange)
//    val onTextChangeRef = remember(weakRefOf(onTextChangeUpdated)) {
//        LazyDelegate { onTextChangeUpdated }
//    }
    val onTextChangeRef by rememberUpdatedState(weakRefOf(onTextChange))

//    log.v { "text = |${text.buildString()}|" }
//    log.v { "transformedText = |${transformedText.buildString()}|" }

    remember(weakRefOf(text), viewState) {
        text.undoMetadataSupplier = {
            BigTextUndoMetadata(
                cursor = viewState.cursorIndex,
                selection = viewState.selection,
            )
        }
    }

    fun fireOnLayout() {
        lineHeight = (textLayouter.charMeasurer as ComposeUnicodeCharMeasurer).getRowHeight()
        log.d { "fireOnLayout lineHeight=$lineHeight" }
        val layoutResult = BigTextSimpleLayoutResult(
            text = transformedText, // layout is only performed in `transformedText`
            rowHeight = lineHeight,
        ).also {
            layoutResult = it
            viewState.layoutResult = it
        }
        onTextLayout?.invoke(layoutResult)
        forceRecompose = Random.nextLong()
    }

    if (isLayoutEnabled && contentWidth > 0 && isContentWidthLatest) {
        remember(weakRefOf(transformedText), textLayouter, contentWidth, isSoftWrapEnabled) {
            log.d { "CoreBigMonospaceText set contentWidth = $contentWidth" }
            val transformedText = transformedTextRef.get() ?: return@remember

            val layout = layout@ {
                log.d { "BigText start layout" }
                val startInstant = KInstant.now()

                heavyCompute {
                    transformedText.onLayoutCallback = {
                        // this callback actually will be invoked by transformedText.setContentWidth()
                        coroutineScope.launch {
                            fireOnLayout()
                        }
                    }
                    transformedText.setSoftWrapEnabled(isSoftWrapEnabled)
                    transformedText.setLayouter(textLayouter)
                    transformedText.setContentWidth(contentWidth)

                    val endInstant = KInstant.now()
                    log.i { "BigText layout took ${endInstant - startInstant} at ${Thread.currentThread().name}" }

                    if (log.config.minSeverity <= Severity.Verbose) {
                        (transformedText as? BigTextImpl)?.printDebug("after init layout")
                    }

                    transformedText.onLayoutCallback?.invoke()
                }
            }
            if (transformedText.isThreadSafe) {
                // TODO: support asynchronous layout without illegal states / race conditions and blocking locks. consider query calls by BigText consumers.
//                Thread {
                    layout()
//                }.start()
            } else {
                layout()
            }
        }
    }

    if (isComponentReady()) {
        rememberLast(height, transformedText.numOfRows, lineHeight) {
            scrollState::class.declaredMemberProperties.first { it.name == "maxValue" }
                .apply {
                    (this as KMutableProperty<Int>)
                    setter.isAccessible = true
                    val scrollableHeight = maxOf(
                        0f,
                        transformedText.numOfRows * lineHeight - height +
                                with(density) {
                                    (padding.calculateTopPadding() + padding.calculateBottomPadding()).toPx()
                                }
                    )
                    setter.call(scrollState, scrollableHeight.roundToInt())
                }

            scrollState::class.declaredMemberProperties.first { it.name == "viewportSize" }
                .apply {
                    (this as KMutableProperty<Int>)
                    setter.isAccessible = true
                    setter.call(scrollState, height)
                }
        }

        rememberLast(width, transformedText.maxLineWidth) {
            horizontalScrollState::class.declaredMemberProperties.first { it.name == "maxValue" }
                .apply {
                    (this as KMutableProperty<Int>)
                    setter.isAccessible = true
                    val scrollableWidth = maxOf(
                        0f,
                        (transformedText.maxLineWidth / transformedText.widthMultiplier.toFloat()) - width +
                                with(density) {
                                    2 * (padding.calculateLeftPadding(LayoutDirection.Ltr) + padding.calculateRightPadding(
                                        LayoutDirection.Ltr
                                    )).toPx() +
                                            20.dp.toPx()
                                }
                    )
                    log.d { "scrollableWidth = $scrollableWidth, maxLineWidth = ${transformedText.maxLineWidth}, width = $width" }
                    setter.call(horizontalScrollState, scrollableWidth.roundToInt())
                }

            horizontalScrollState::class.declaredMemberProperties.first { it.name == "viewportSize" }
                .apply {
                    (this as KMutableProperty<Int>)
                    setter.isAccessible = true
                    val viewportLength = width -
                            with(density) {
                                (padding.calculateLeftPadding(LayoutDirection.Ltr) + padding.calculateRightPadding(
                                    LayoutDirection.Ltr
                                )).toPx()
                            }
                    setter.call(horizontalScrollState, viewportLength.roundToInt())
                }
        }
    }

    var transformedState by remember(weakRefOf(text), textTransformation) {
        mutableStateOf<Any?>(null)
    }
    remember(weakRefOf(text), textTransformation) {
//        log.v { "CoreBigMonospaceText text = |${text.buildString()}|" }
        if (textTransformation != null) {
            log.d { "CoreBigMonospaceText start init transform" }
            heavyCompute {
                val startInstant = KInstant.now()
                textTransformation.initialize(text, transformedText).also {
                    val endInstant = KInstant.now()
                    log.d { "CoreBigMonospaceText init transformedState ${it.hashCode()} took ${endInstant - startInstant}" }
                    if (log.config.minSeverity <= Severity.Verbose) {
                        transformedText.printDebug("init transformedState")
                    }
                    withContext(coroutineScope.coroutineContext) {
                        viewState.transformedText = weakRefOf(transformedText)
                        transformedState = it
                        isTransformedStateReady = true
                        onTransformInit?.invoke(transformedText)
                    }
                }
            }
        } else {
            viewState.transformedText = weakRefOf(transformedText)
            transformedState = null
            isTransformedStateReady = true
        }
    }

    remember(weakRefOf(text), textDecorator) {
        if (textDecorator != null) {
            heavyCompute {
                val startInstant = KInstant.now()
                textDecorator.initialize(text).also {
                    val endInstant = KInstant.now()
                    log.i { "CoreBigMonospaceText init textDecorator took ${endInstant - startInstant}" }
                }
            }
        }
    }

    if (textTransformation != null) {
        viewState.pollReapplyTransformCharRanges().forEach {
            log.d { "onReapplyTransform $it" }
            val startInstant = KInstant.now()
            (textTransformation as IncrementalTextTransformation<Any?>)
                .onReapplyTransform(text, it, transformedText, transformedState)
            log.d { "onReapplyTransform done ${KInstant.now() - startInstant}" }
        }
    }

    rememberLast(viewState.selection.start, viewState.selection.last, textTransformation) {
        viewState.transformedSelection = if (viewState.hasSelection()) {
            transformedText.findTransformedPositionByOriginalPosition(viewState.selection.start) ..
                    transformedText.findTransformedPositionByOriginalPosition(maxOf(0, viewState.selection.last))
        } else {
            IntRange.EMPTY
        }
    }

    val scrollableState = rememberScrollableState { delta ->
        coroutineScope.launch {
            scrollState.scrollBy(-delta)
        }
        val predictConsume = if (delta < 0) { // downwards
            -minOf(
                (scrollState.maxValue - scrollState.value).toFloat().coerceAtLeast(0f),
                -delta
            )
        } else { // upwards
            minOf(
                (scrollState.value.toFloat() - 0).coerceAtLeast(0f),
                delta
            )
        }
//        log.v { "predictConsume=$predictConsume m=${(scrollState.maxValue - scrollState.value)} d=$delta" }
        predictConsume
    }
    val windowInfo = LocalWindowInfo.current
    var dragStartViewportTop by remember { mutableStateOf(0f) }
    var dragStartViewportLeft by remember { mutableStateOf(0f) }
    var dragStartPoint by remember { mutableStateOf<Offset>(Offset.Zero) }
    var draggedPoint by remember { mutableStateOf<Offset>(Offset.Zero) }
    var draggedPointAccumulated by remember { mutableStateOf<Offset>(Offset.Zero) }
    var dragOffset by remember { mutableStateOf<Offset>(Offset.Zero) }
    var continuousDragOffset by remember { mutableStateOf<Offset>(Offset.Zero) }
    var continuousDragLastMoveTime by remember { mutableStateOf(KInstant.now()) }
    var selectionEnd by remember { mutableStateOf<Int>(-1) }
    var isFocused by remember { mutableStateOf(false) }

    var isShowContextMenu by remember { mutableStateOf(false) }

    val viewportTop by rememberUpdatedState(scrollState.value.toFloat())
    val viewportLeft by rememberUpdatedState(if (isSoftWrapEnabled) 0f else horizontalScrollState.value.toFloat())

    fun getTransformedCharIndex(x: Float, y: Float, mode: ResolveCharPositionMode): Int {
        val transformedText = transformedTextRef.get() ?: return 0
        val row = ((viewportTop + y) / lineHeight).toInt()
        val maxIndex = maxOf(0, transformedText.length /*- if (mode == ResolveCharPositionMode.Selection) 1 else 0*/)
        if (row > transformedText.lastRowIndex) {
            return maxIndex
        } else if (row < 0) {
            return 0
        }

        val rowPositionStart = transformedText.findRowPositionStartIndexByRowIndex(row)
        val nextRowPositionStart = if (row + 1 <= transformedText.lastRowIndex) {
            transformedText.findRowPositionStartIndexByRowIndex(row + 1)
        } else {
            transformedText.length + 1
        }
        val lineIndex = transformedText.findLineIndexByRowIndex(row)
//        val linePositionStart = transformedText.findPositionStartOfLine(lineIndex)
//        val rowPositionOffset = rowPositionStart - linePositionStart
        val absX = (viewportLeft + x).toInt()
        log.v { "viewportLeft=$viewportLeft, x=$x, absX=$absX, r=$row, l=$lineIndex, rs=$rowPositionStart, nrs=$nextRowPositionStart" }
        val pos = transformedText.findMaxEndPositionOfWidthSumOverPositionRangeAtMost(
            startPosition = rowPositionStart,
            endPositions = rowPositionStart ..< nextRowPositionStart,
            isEndExclusive = true,
            maxWidthSum = absX,
        )
        log.v { "pos=$pos" }
        return pos.coerceIn(0 .. maxIndex)
    }

    fun getTransformedStringWidth(start: Int, endExclusive: Int): Float {
        val transformedText = transformedTextRef.get() ?: return 0f

        return transformedText.findWidthByPositionRangeOfSameLine(start .. endExclusive - 1)
//        return (start .. endExclusive - 1)
//            .map {
//                val char = transformedText.substring(it..it).string()
//                if (char == "\n") { // selecting \n shows a narrow width
//                    textLayouter.charMeasurer.findCharWidth(" ")
//                } else {
//                    textLayouter.charMeasurer.findCharWidth(char)
//                }
//            }
//            .sum()
    }

    fun showCursor() {
        cursorShowTrigger.trySend(Unit)
    }

    fun generateChangeEvent(eventType: BigTextChangeEventType, changeStartIndex: Int, changeEndExclusiveIndex: Int) : BigTextChangeEvent? {
        val text = textRef.get() ?: return null
        return BigTextChangeEvent(
            changeId = viewState.version,
            bigText = text,
            eventType = eventType,
            changeStartIndex = changeStartIndex,
            changeEndExclusiveIndex = changeEndExclusiveIndex,
        )
    }

    fun scrollToCursor(): Boolean = with(density) {
        if (!isComponentReady()) return false
        val transformedText = transformedTextRef.get() ?: return false
        val row = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)

        if (!isSoftWrapEnabled) {
            val extraPadding = 8.dp // e.g. width of scroll bar
            val visibleHorizontalRange = horizontalScrollState.value + (padding.calculateLeftPadding(LayoutDirection.Ltr) + extraPadding).toPx() ..<
                horizontalScrollState.value + width - (padding.calculateRightPadding(LayoutDirection.Ltr) + extraPadding).toPx()
            val linePositionStart = transformedText.findPositionStartOfLine(row)
            // cursor is right after the character at (viewState.transformedCursorIndex - 1)
            val cursorXInLine = transformedText.findWidthByPositionRangeOfSameLine(linePositionStart .. viewState.transformedCursorIndex - 1)
            log.d { "vhr=$visibleHorizontalRange cx=$cursorXInLine h=${horizontalScrollState.value}" }
            if (cursorXInLine !in visibleHorizontalRange) {
                // scroll to an offset with a little space
                val scrollToPosition = if (cursorXInLine < visibleHorizontalRange.start) {
                    cursorXInLine - (padding.calculateLeftPadding(LayoutDirection.Ltr) + extraPadding + 12.dp).toPx()
                } else {
                    cursorXInLine + (12.dp + padding.calculateRightPadding(LayoutDirection.Ltr) + extraPadding).toPx() - width
                }.roundToInt().coerceIn(0 ..< maxOf(1, horizontalScrollState.maxValue))
                log.d { "scroll to $scrollToPosition" }
                coroutineScope.launch {
                    horizontalScrollState.animateScrollTo(scrollToPosition)
                }
            }
        }

        val layoutResult = layoutResult ?: return false

        // scroll to cursor position if out of visible range
        val visibleVerticalRange = scrollState.value .. scrollState.value + height
        val rowVerticalRange = layoutResult.getTopOfRow(row).toInt() .. layoutResult.getBottomOfRow(row).toInt()
        if (rowVerticalRange !in visibleVerticalRange) {
            val scrollToPosition = if (rowVerticalRange.start < visibleVerticalRange.start) {
                rowVerticalRange.start
            } else {
                // scroll to a position that includes the bottom of the row + a little space
                minOf(layoutResult.bottom.toInt(), maxOf(0, rowVerticalRange.endInclusive + maxOf(2, (layoutResult.rowHeight * 0.5).toInt()) - height))
            }
            coroutineScope.launch {
                scrollState.animateScrollTo(scrollToPosition)
            }
        }

        true
    }

    fun recordCursorXPosition() {
//        val transformedText = transformedTextRef.get() ?: return
//        val row = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)
//        val rowStart = transformedText.findRowPositionStartIndexByRowIndex(row)
//        log.d { "recordCursorXPosition Tcur=${viewState.transformedCursorIndex} row=$row rowStart=$rowStart" }
//        val cursorXPosInRow = transformedText.findWidthByPositionRangeOfSameLine(rowStart ..< viewState.transformedCursorIndex)
//        viewState.lastCursorXPositionForVerticalMovement = cursorXPosInRow
        viewState.recordCursorXPosition()
    }

    fun updateViewState() {
        val transformedText = transformedTextRef.get() ?: return
        viewState.lastVisibleRow = minOf(viewState.lastVisibleRow, transformedText.lastRowIndex)
        log.d { "lastVisibleRow = ${viewState.lastVisibleRow}, lastRowIndex = ${transformedText.lastRowIndex}" }
    }

    fun onValuePreChange(eventType: BigTextChangeEventType, changeStartIndex: Int, changeEndExclusiveIndex: Int) {
        val transformedText = transformedTextRef.get() ?: return
        viewState.version = Random.nextLong()
        val event = generateChangeEvent(eventType, changeStartIndex, changeEndExclusiveIndex) ?: return

        // invoke textTransformation listener before deletion, so that it knows what will be deleted and transform accordingly
        (textTransformation as? IncrementalTextTransformation<Any?>)?.beforeTextChange(
            event,
            transformedText,
            transformedState
        )
        textDecorator?.beforeTextChange(event)
    }

    fun onValuePostChange(eventType: BigTextChangeEventType, changeStartIndex: Int, changeEndExclusiveIndex: Int) {
        val transformedText = transformedTextRef.get() ?: return
        updateViewState()

        viewState.version = Random.nextLong()
        val event = generateChangeEvent(eventType, changeStartIndex, changeEndExclusiveIndex) ?: return
        (textTransformation as? IncrementalTextTransformation<Any?>)?.afterTextChange(
            event,
            transformedText,
            transformedState
        )
        textDecorator?.afterTextChange(event)
        
        val onTextChange = onTextChangeRef.get() ?: return
        log.d { "call onTextChange for ${event.changeId}" }
        onTextChange(event)
    }

    fun delete(start: Int, endExclusive: Int) {
        if (start >= endExclusive) {
            return
        }
        val text = textRef.get() ?: return
//        onValuePreChange(BigTextChangeEventType.Delete, start, endExclusive)
        text.delete(start, endExclusive)
//        onValuePostChange(BigTextChangeEventType.Delete, start, endExclusive)
    }

    fun deleteSelection(isSaveUndoSnapshot: Boolean) {
        val text = textRef.get() ?: return
        val transformedText = transformedTextRef.get() ?: return
        if (viewState.hasSelection()) {
            val start = viewState.selection.start
            val endExclusive = viewState.selection.endInclusive + 1
            delete(start, endExclusive)

            viewState.selection = EMPTY_SELECTION_RANGE // cannot use IntRange.EMPTY as `viewState.selection.start` is in use
            viewState.transformedSelection = EMPTY_SELECTION_RANGE
            viewState.cursorIndex = start
            viewState.updateTransformedCursorIndexByOriginal(transformedText)
            viewState.transformedSelectionStart = viewState.transformedCursorIndex

            if (isSaveUndoSnapshot) {
                text.recordCurrentChangeSequenceIntoUndoHistory()
            }
            recordCursorXPosition()
        }
    }

    fun insertAt(insertPos: Int, textInput: CharSequence): CharSequence {
        val text = textRef.get() ?: return ""
        var textInput = inputFilter?.filter(textInput) ?: textInput
        if (isSingleLineInput) {
            val find = NEW_LINE_REGEX.find(textInput)
            if (find != null) {
                textInput = if (textInput is AnnotatedString) {
                    textInput.replaceAll(NEW_LINE_REGEX, " ", find.range.start)
                } else {
                    textInput.replace(NEW_LINE_REGEX, " ")
                }
            }
        }
        val currentLength = text.length
        if (currentLength + textInput.length > maxInputLength) {
            if (currentLength + 1 <= maxInputLength) { // there is still room to input text
                textInput = textInput.subSequence(0, (maxInputLength - currentLength).toInt())
            } else {
                textInput = ""
            }
        }

        if (textInput.isNotEmpty()) {
//            onValuePreChange(BigTextChangeEventType.Insert, insertPos, insertPos + textInput.length)
            text.insertAt(insertPos, textInput)
//            onValuePostChange(BigTextChangeEventType.Insert, insertPos, insertPos + textInput.length)
        }
        return textInput
    }

    fun onType(textInput: CharSequence, isSaveUndoSnapshot: Boolean = true) {
        val text = textRef.get() ?: return
        val transformedText = transformedTextRef.get() ?: return
        log.v { "$text key in '$textInput' ${viewState.hasSelection()} ${viewState.selection} ${viewState.transformedSelection}" }
        var hasManipulatedText = false
        if (viewState.hasSelection()) {
            deleteSelection(isSaveUndoSnapshot = false)
            hasManipulatedText = true
        }
        val insertPos = viewState.cursorIndex
        val textInput = insertAt(insertPos, textInput)
        if (textInput.isNotEmpty()) {
            hasManipulatedText = true
        }
        if (!hasManipulatedText) {
            return
        }
        updateViewState()
        if (log.config.minSeverity <= Severity.Verbose) {
            (transformedText as? BigTextImpl)?.printDebug("transformedText onType '${textInput.string().replace("\n", "\\n")}'")
        }
        // update cursor after invoking listeners, because a transformation or change may take place
        viewState.cursorIndex = minOf(text.length, insertPos + textInput.length)
        viewState.updateTransformedCursorIndexByOriginal(transformedText)
        viewState.transformedSelectionStart = viewState.transformedCursorIndex
        log.v { "set cursor pos 2 => ${viewState.cursorIndex} t ${viewState.transformedCursorIndex}" }
        if (isSaveUndoSnapshot) {
            text.recordCurrentChangeSequenceIntoUndoHistory()
        }
        recordCursorXPosition()
        scrollToCursor()
        showCursor()
    }

    fun onDelete(direction: TextFBDirection): Boolean {
        val text = textRef.get() ?: return false
        val transformedText = transformedTextRef.get() ?: return false
        val cursor = viewState.cursorIndex

        if (viewState.hasSelection()) {
            deleteSelection(isSaveUndoSnapshot = true)
            updateViewState()
            return true
        }

        when (direction) {
            TextFBDirection.Forward -> {
                if (cursor + 1 <= text.length) {
//                    onValuePreChange(BigTextChangeEventType.Delete, cursor, cursor + 1)
                    text.delete(cursor, cursor + 1)
//                    onValuePostChange(BigTextChangeEventType.Delete, cursor, cursor + 1)
                    updateViewState()
                    if (log.config.minSeverity <= Severity.Verbose) {
                        (transformedText as? BigTextImpl)?.printDebug("transformedText onDelete $direction")
                    }
                    text.recordCurrentChangeSequenceIntoUndoHistory()
                    recordCursorXPosition()
                    scrollToCursor()
                    return true
                }
            }
            TextFBDirection.Backward -> {
                if (cursor - 1 >= 0) {
//                    onValuePreChange(BigTextChangeEventType.Delete, cursor - 1, cursor)
                    text.delete(cursor - 1, cursor)
//                    onValuePostChange(BigTextChangeEventType.Delete, cursor - 1, cursor)
                    updateViewState()
                    if (log.config.minSeverity <= Severity.Verbose) {
                        (transformedText as? BigTextImpl)?.printDebug("transformedText onDelete $direction")
                    }
                    // update cursor after invoking listeners, because a transformation or change may take place
                    viewState.cursorIndex = maxOf(0, cursor - 1)
                    viewState.updateTransformedCursorIndexByOriginal(transformedText)
                    viewState.transformedSelectionStart = viewState.transformedCursorIndex
                    log.v { "set cursor pos 3 => ${viewState.cursorIndex} t ${viewState.transformedCursorIndex}" }
                    text.recordCurrentChangeSequenceIntoUndoHistory()
                    recordCursorXPosition()
                    scrollToCursor()
                    return true
                }
            }
        }
        return false
    }

    fun onUndoRedo(operation: (BigTextChangeCallback) -> Pair<Boolean, Any?>) {
        val transformedText = transformedTextRef.get() ?: return
        var lastChangeEnd = -1
        val stateToBeRestored = operation(object : BigTextChangeCallback {
            override fun onValuePreChange(
                eventType: BigTextChangeEventType,
                changeStartIndex: Int,
                changeEndExclusiveIndex: Int
            ) {
                // moved to listener
//                onValuePreChange(eventType, changeStartIndex, changeEndExclusiveIndex)
            }

            override fun onValuePostChange(
                eventType: BigTextChangeEventType,
                changeStartIndex: Int,
                changeEndExclusiveIndex: Int
            ) {
                // moved to listener
//                onValuePostChange(eventType, changeStartIndex, changeEndExclusiveIndex)

                // no longer in use
//                lastChangeEnd = when (eventType) {
//                    BigTextChangeEventType.Insert -> changeEndExclusiveIndex
//                    BigTextChangeEventType.Delete -> changeStartIndex
//                }
            }
        })
        updateViewState()
        (stateToBeRestored.second as? BigTextUndoMetadata)?.let { state ->
            viewState.selection = state.selection
            viewState.updateTransformedSelectionBySelection(transformedText)
            viewState.cursorIndex = state.cursor
            viewState.updateTransformedCursorIndexByOriginal(transformedText)
            viewState.transformedSelectionStart = viewState.transformedCursorIndex
            recordCursorXPosition()
            scrollToCursor()
            return
        }
        if (lastChangeEnd >= 0) { // this `if` should never execute
            viewState.cursorIndex = lastChangeEnd
            viewState.updateTransformedCursorIndexByOriginal(transformedText)
            viewState.transformedSelectionStart = viewState.transformedCursorIndex
        }
    }

    fun undo() {
        val text = textRef.get() ?: return
        onUndoRedo { text.undo(it) }
    }

    fun redo() {
        val text = textRef.get() ?: return
        onUndoRedo { text.redo(it) }
    }

    fun copySelection() {
        val text = textRef.get() ?: return
        if (!viewState.hasSelection()) {
            return
        }

        val textToCopy = text.substring(
            viewState.selection.first.. viewState.selection.last
        )
        clipboardManager.setText(textToCopy.annotatedString())
    }

    fun cutSelection() {
        if (!viewState.hasSelection()) {
            return
        }

        copySelection()
        deleteSelection(isSaveUndoSnapshot = true)
    }

    fun paste(): Boolean {
        val textToPaste = clipboardManager.getText()?.text
        return if (!textToPaste.isNullOrEmpty()) {
            onType(textToPaste)
            true
        } else {
            false
        }
    }

    fun selectAll() {
        val text = textRef.get() ?: return
        val transformedText = transformedTextRef.get() ?: return
        if (text.isNotEmpty) {
            viewState.selection = 0..text.lastIndex
            viewState.updateTransformedSelectionBySelection(transformedText)
        }
    }

    fun findPreviousWordBoundaryPositionFromCursor(isIncludeCursorPosition: Boolean = false): Int {
        val text = textRef.get() ?: return 0
        val transformedText = transformedTextRef.get() ?: return 0

        val currentRowIndex = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)
        val transformedRowStart = transformedText.findRowPositionStartIndexByRowIndex(currentRowIndex)
        val rowStart = transformedText.findOriginalPositionByTransformedPosition(transformedRowStart)
        val end = minOf(text.length, viewState.cursorIndex + if (isIncludeCursorPosition) 1 else 0)
        val substringFromRowStartToCursor = text.substring(rowStart, end)
        if (substringFromRowStartToCursor.isEmpty()) {
            return maxOf(0, rowStart - 1)
        }
        val wordBoundaryAt = "\\b".toRegex().findAll(substringFromRowStartToCursor)
            .filter { it.range.start < substringFromRowStartToCursor.length }
            .lastOrNull()?.range?.start ?: 0
        return rowStart + wordBoundaryAt
    }

    fun findNextWordBoundaryPositionFromCursor(): Int {
        val text = textRef.get() ?: return 0
        val transformedText = transformedTextRef.get() ?: return 0

        val currentRowIndex = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)
        val transformedRowEnd = if (currentRowIndex + 1 <= transformedText.lastRowIndex) {
            transformedText.findRowPositionStartIndexByRowIndex(currentRowIndex + 1)
        } else {
            transformedText.length
        }
        val rowEnd = transformedText.findOriginalPositionByTransformedPosition(transformedRowEnd)
        val substringFromCursorToRowEnd = text.substring(viewState.cursorIndex, rowEnd)
        if (substringFromCursorToRowEnd.isEmpty()) {
            return minOf(text.length, rowEnd)
        }
        val wordBoundaryAt = "\\b".toRegex().findAll(substringFromCursorToRowEnd)
            .filter { it.range.start > 0 }
            .firstOrNull()?.range?.start ?: substringFromCursorToRowEnd.length
        return viewState.cursorIndex + wordBoundaryAt
    }

    fun updateOriginalCursorOrSelection(newPosition: Int, isSelection: Boolean) {
        val transformedText = transformedTextRef.get() ?: return

        val oldCursorPosition = viewState.cursorIndex
        viewState.cursorIndex = newPosition
        viewState.updateTransformedCursorIndexByOriginal(transformedText)
        if (isSelection) {
            val selectionStart = if (viewState.hasSelection()) {
                transformedText.findOriginalPositionByTransformedPosition(viewState.transformedSelectionStart)
            } else {
                oldCursorPosition
            }
            viewState.selection = minOf(selectionStart, newPosition) until maxOf(selectionStart, newPosition)
            viewState.updateTransformedSelectionBySelection(transformedText)
        } else {
            viewState.transformedSelectionStart = viewState.transformedCursorIndex
            viewState.transformedSelection = IntRange.EMPTY
        }
        scrollToCursor()
    }

    fun updateTransformedCursorOrSelection(newTransformedPosition: Int, isSelection: Boolean) {
        val transformedText = transformedTextRef.get() ?: return
        val oldTransformedCursorPosition = viewState.transformedCursorIndex
        viewState.transformedCursorIndex = newTransformedPosition
        viewState.updateCursorIndexByTransformed(transformedText)
        if (isSelection) {
            val selectionTransformedStart = if (viewState.hasSelection()) {
                viewState.transformedSelectionStart
            } else {
                oldTransformedCursorPosition
            }
            log.d { "select T $selectionTransformedStart ~ $newTransformedPosition" }
            viewState.transformedSelection = minOf(selectionTransformedStart, newTransformedPosition) until maxOf(selectionTransformedStart, newTransformedPosition)
            viewState.updateSelectionByTransformedSelection(transformedText)
        } else {
            viewState.transformedSelectionStart = viewState.transformedCursorIndex
            viewState.transformedSelection = IntRange.EMPTY
        }
        scrollToCursor()
    }

    fun processKeyboardInput(it: KeyEvent): Boolean {
        val text = textRef.get() ?: return false
        val transformedText = transformedTextRef.get() ?: return false
        return when {
            it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && it.key == Key.C && !viewState.transformedSelection.isEmpty() -> {
                // Hit Ctrl-C or Cmd-C to copy
                log.d { "BigMonospaceText hit copy" }
                copySelection()
                true
            }
            it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && it.key == Key.X && !viewState.transformedSelection.isEmpty() -> {
                // Hit Ctrl-X or Cmd-X to cut
                log.d { "BigMonospaceText hit cut" }
                cutSelection()
                true
            }
            isEditable && it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && it.key == Key.V -> {
                // Hit Ctrl-V or Cmd-V to paste
                log.d { "BigMonospaceTextField hit paste" }
                paste()
            }
            isEditable && it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && !it.isShiftPressed && it.key == Key.Z -> {
                // Hit Ctrl-Z or Cmd-Z to undo
                log.d { "BigMonospaceTextField hit undo" }
                undo()
                true
            }
            isEditable && it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && it.isShiftPressed && it.key == Key.Z -> {
                // Hit Ctrl-Shift-Z or Cmd-Shift-Z to redo
                log.d { "BigMonospaceTextField hit redo" }
                redo()
                true
            }
            /* selection */
            it.type == KeyEventType.KeyDown && it.isCtrlOrCmdPressed() && it.key == Key.A -> {
                // Hit Ctrl-A or Cmd-A to select all
                selectAll()
                true
            }
            /* text input */
            isEditable && it.isTypedEvent -> {
                log.v { "key type '${it.key}'" }
                val textInput = it.toTextInput()
                if (textInput != null) {
                    onType(textInput)
                    true
                } else {
                    false
                }
            }
            isEditable && it.type == KeyEventType.KeyDown -> when {
                it.key == Key.Enter && !it.isShiftPressed && !it.isCtrlPressed && !it.isAltPressed && !it.isMetaPressed -> {
                    if (isSingleLineInput) {
                        false
                    } else {
                        onType("\n")
                        true
                    }
                }
                it.key == Key.Backspace -> when {
                    (currentOS() == MacOS && it.isAltPressed) ||
                        (currentOS() != MacOS && it.isCtrlPressed) -> {
                            // delete previous word
                            val previousWordPosition = findPreviousWordBoundaryPositionFromCursor()
                            if (previousWordPosition >= viewState.cursorIndex) {
                                return false
                            }
                            delete(previousWordPosition, viewState.cursorIndex)
                            updateViewState()
                            // update cursor after invoking listeners, because a transformation or change may take place
                            viewState.cursorIndex = previousWordPosition
                            viewState.updateTransformedCursorIndexByOriginal(transformedText)
                            viewState.transformedSelectionStart = viewState.transformedCursorIndex
                            recordCursorXPosition()
                            text.recordCurrentChangeSequenceIntoUndoHistory()
                            true
                        }
                    else -> onDelete(TextFBDirection.Backward)
                }
                it.key == Key.Delete -> {
                    onDelete(TextFBDirection.Forward)
                }
                /* text navigation */
                (currentOS() == MacOS && it.isMetaPressed && it.key == Key.DirectionUp) ||
                        (currentOS() != MacOS && it.isCtrlPressed && it.key == Key.MoveHome) -> {
                    updateOriginalCursorOrSelection(newPosition = 0, isSelection = it.isShiftPressed)
                    showCursor()
                    true
                }
                (currentOS() == MacOS && it.isMetaPressed && it.key == Key.DirectionDown) ||
                        (currentOS() != MacOS && it.isCtrlPressed && it.key == Key.MoveEnd) -> {
                    updateOriginalCursorOrSelection(newPosition = text.length, isSelection = it.isShiftPressed)
                    showCursor()
                    true
                }
                (currentOS() == MacOS && it.isMetaPressed && it.key in listOf(Key.DirectionLeft, Key.DirectionRight)) ||
                        it.key in listOf(Key.MoveHome, Key.MoveEnd) -> {
                    // use `transformedText` as basis because `text` does not perform layout
                    val currentRowIndex = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)
                    val newTransformedPosition = if (it.key in listOf(Key.DirectionLeft, Key.MoveHome)) {
                        // home -> move to start of row
                        log.d { "move to start of row $currentRowIndex" }
                        transformedText.findRowPositionStartIndexByRowIndex(currentRowIndex)
                    } else {
                        // end -> move to end of row
                        log.d { "move to end of row $currentRowIndex" }
                        if (currentRowIndex + 1 <= transformedText.lastRowIndex) {
                            transformedText.findRowPositionStartIndexByRowIndex(currentRowIndex + 1) - /* the '\n' char */ 1
                        } else {
                            transformedText.length
                        }
                    }
                    updateTransformedCursorOrSelection(
                        newTransformedPosition = newTransformedPosition,
                        isSelection = it.isShiftPressed,
                    )
                    recordCursorXPosition()
                    showCursor()
                    true
                }
                it.key == Key.DirectionLeft && (
                        (currentOS() == MacOS && it.isAltPressed) ||
                                (currentOS() != MacOS && it.isCtrlPressed)
                        ) -> {
                    val newPosition = findPreviousWordBoundaryPositionFromCursor()
                    updateOriginalCursorOrSelection(newPosition = newPosition, isSelection = it.isShiftPressed)
                    recordCursorXPosition()
                    showCursor()
                    true
                }
                it.key == Key.DirectionRight && (
                        (currentOS() == MacOS && it.isAltPressed) ||
                                (currentOS() != MacOS && it.isCtrlPressed)
                        ) -> {
                    val newPosition = findNextWordBoundaryPositionFromCursor()
                    updateOriginalCursorOrSelection(newPosition = newPosition, isSelection = it.isShiftPressed)
                    recordCursorXPosition()
                    showCursor()
                    true
                }
                it.key in listOf(Key.DirectionLeft, Key.DirectionRight) -> {
                    val delta = if (it.key == Key.DirectionRight) 1 else -1
                    if (viewState.transformedCursorIndex + delta in 0 .. transformedText.length) {
                        var newTransformedPosition = viewState.transformedCursorIndex + delta
                        newTransformedPosition = if (delta > 0) {
                            viewState.roundedTransformedCursorIndex(newTransformedPosition, CursorAdjustDirection.Forward, transformedText, viewState.transformedCursorIndex /* FIXME IndexOutOfBoundsException */, false)
                        } else {
                            viewState.roundedTransformedCursorIndex(newTransformedPosition, CursorAdjustDirection.Backward, transformedText, newTransformedPosition, true)
                        }
                        updateTransformedCursorOrSelection(
                            newTransformedPosition = newTransformedPosition,
                            isSelection = it.isShiftPressed,
                        )
                        recordCursorXPosition()
                        log.v { "set cursor pos LR => ${viewState.cursorIndex} t ${viewState.transformedCursorIndex}" }
                    } else if (!it.isShiftPressed) {
                        // cancel selection
                        val newTransformedPosition = if (viewState.hasSelection()) {
                            if (delta > 0) {
                                viewState.transformedSelection.endInclusive + 1
                            } else {
                                viewState.transformedSelection.start
                            }
                        } else {
                            viewState.transformedCursorIndex
                        }
                        updateTransformedCursorOrSelection(
                            newTransformedPosition = newTransformedPosition,
                            isSelection = it.isShiftPressed,
                        )
                        recordCursorXPosition()
                    }
                    showCursor()
                    true
                }
                it.key in listOf(Key.DirectionUp, Key.DirectionDown) -> {
                    val row = transformedText.findRowIndexByPosition(viewState.transformedCursorIndex)
                    val newRow = row + if (it.key == Key.DirectionDown) 1 else -1
                    var isRecordCursorXPosition = false
                    var newTransformedPosition = Unit.let {
                        if (newRow < 0) {
                            isRecordCursorXPosition = true
                            0
                        } else if (newRow > transformedText.lastRowIndex) {
                            isRecordCursorXPosition = true
                            transformedText.length
                        } else {
                            /* // this is only correct for Monospace
                            val col = viewState.transformedCursorIndex - transformedText.findRowPositionStartIndexByRowIndex(row)
                            val newRowLength = if (newRow + 1 <= transformedText.lastRowIndex) {
                                transformedText.findRowPositionStartIndexByRowIndex(newRow + 1) - 1
                            } else {
                                transformedText.length
                            } - transformedText.findRowPositionStartIndexByRowIndex(newRow)
                            val pos = if (col <= newRowLength) {
                                transformedText.findRowPositionStartIndexByRowIndex(newRow) + col
                            } else {
                                transformedText.findRowPositionStartIndexByRowIndex(newRow) + newRowLength
                            }*/
//                            val rowStart = transformedText.findRowPositionStartIndexByRowIndex(row)
//                            val cursorXPosInRow = transformedText.findWidthByPositionRangeOfSameLine(rowStart ..< viewState.transformedCursorIndex)
                            val newRowStart = transformedText.findRowPositionStartIndexByRowIndex(newRow)
                            val newRowEndInclusive = if (newRow + 1 <= transformedText.lastRowIndex) {
                                transformedText.findRowPositionStartIndexByRowIndex(newRow + 1) - 1
                            } else {
                                transformedText.length
                            }
                            val pos = transformedText.findMaxEndPositionOfWidthSumOverPositionRangeAtMost(
                                startPosition = newRowStart,
                                endPositions = newRowStart..newRowEndInclusive,
                                isEndExclusive = true,
                                maxWidthSum = viewState.lastCursorXPositionForVerticalMovement.roundToInt() //cursorXPosInRow.toInt()
                            )
                            if (pos > 0) {
                                viewState.roundedTransformedCursorIndex(
                                    pos,
                                    CursorAdjustDirection.Bidirectional,
                                    transformedText,
                                    pos - 1,
                                    true
                                )
                            } else {
                                pos
                            }
                        }
                    }
                    updateTransformedCursorOrSelection(
                        newTransformedPosition = newTransformedPosition,
                        isSelection = it.isShiftPressed,
                    )
                    if (isRecordCursorXPosition) {
                        recordCursorXPosition()
                    }
                    showCursor()
                    true
                }
                else -> false
            }
            else -> false
        }
    }

    fun onProcessKeyboardInput(keyEvent: KeyEvent): Boolean {
        val text = textRef.get() ?: return false

        if (keyboardInputProcessor?.beforeProcessInput(keyEvent, viewState) == true) {
            return true
        }
        var result = processKeyboardInput(keyEvent)
        if (keyboardInputProcessor?.afterProcessInput(keyEvent, viewState) == true) {
            result = true
        }
        return result
    }

    fun onDragSelectAndScroll(dragAdditionalOffset: Offset = Offset.Zero) {
        val transformedText = transformedTextRef.get() ?: return
        if (transformedText.isEmpty) {
            viewState.transformedSelection = IntRange.EMPTY
            viewState.selection = EMPTY_SELECTION_RANGE
            viewState.transformedCursorIndex = 0
            viewState.cursorIndex = 0
            return
        }
        val selectionStart = viewState.transformedSelectionStart
        val selectedCharIndex = getTransformedCharIndex(
            x = draggedPointAccumulated.x + dragAdditionalOffset.x + 0f * (dragStartViewportLeft - viewportLeft),
            y = draggedPointAccumulated.y + dragAdditionalOffset.y + 0f * (dragStartViewportTop - viewportTop),
            mode = ResolveCharPositionMode.Selection
        )
            .let {
                if (it >= selectionStart) {
                    viewState.roundedTransformedCursorIndex(it, CursorAdjustDirection.Forward, transformedText, it, true)
                } else {
                    viewState.roundedTransformedCursorIndex(it, CursorAdjustDirection.Backward, transformedText, it, true)
                }
            }
        selectionEnd = selectedCharIndex
        viewState.transformedSelection = minOf(selectionStart, selectionEnd) until maxOf(selectionStart, selectionEnd)
        log.d { "t sel = ${viewState.transformedSelection}" }
        viewState.updateSelectionByTransformedSelection(transformedText)
        viewState.transformedCursorIndex = minOf(
            transformedText.length,
            selectionEnd + if (selectionEnd == viewState.transformedSelection.last) 1 else 0
        )
        viewState.updateCursorIndexByTransformed(transformedText)
        recordCursorXPosition()
        scrollToCursor()
    }

    var textManipulateListener by remember { mutableStateOf<BigTextChangeCallback?>(null) }

    remember(weakRefOf(text)) {
        textManipulateListener?.let {
            text.unregisterCallback(it)
        }
        val listener = object : BigTextChangeCallback {
            override fun onValuePreChange(
                eventType: BigTextChangeEventType,
                changeStartIndex: Int,
                changeEndExclusiveIndex: Int
            ) {
                onValuePreChange(eventType, changeStartIndex, changeEndExclusiveIndex)
            }

            override fun onValuePostChange(
                eventType: BigTextChangeEventType,
                changeStartIndex: Int,
                changeEndExclusiveIndex: Int
            ) {
                onValuePostChange(eventType, changeStartIndex, changeEndExclusiveIndex)
            }
        }
        textManipulateListener = listener
        text.registerCallback(listener)
    }
    
    if (viewState.isScrollToCursorNeeded) {
        if (scrollToCursor()) {
            showCursor()
            viewState.isScrollToCursorNeeded = false
        }
    }

    val tv = remember { TextFieldValue() } // this value is not used

    LaunchedEffect(weakRefOf(transformedText)) {
        val text = textRef.get() ?: return@LaunchedEffect
        val transformedText = transformedTextRef.get() ?: return@LaunchedEffect

        if (log.config.minSeverity <= Severity.Verbose) {
            (0..text.length).forEach {
                log.v { "findTransformedPositionByOriginalPosition($it) = ${transformedText.findTransformedPositionByOriginalPosition(it)}" }
            }

            (0..transformedText.length).forEach {
                log.v { "findOriginalPositionByTransformedPosition($it) = ${transformedText.findOriginalPositionByTransformedPosition(it)}" }
            }
        }
    }

    /**
     * This function is intended to be empty and used as a marker in IDE.
     */
    fun drawUIStart() = Unit

    Box(
        modifier = modifier
            .onGloballyPositioned {
                width = it.size.width
                height = it.size.height
                layoutCoordinates = it
                viewState.visibleSize = Size(width = width, height = height)
                log.v { "BigMonospaceText set width = $width, height = $height" }
            }
            .clipToBounds()
            .padding(padding)
            .runIf(isComponentReady()) {
                scrollable(scrollableState, orientation = Orientation.Vertical)
                    .runIf(!isSoftWrapEnabled) {
                        scrollable(horizontalScrollState, orientation = Orientation.Horizontal, reverseDirection = true)
                    }
            }
            .focusRequester(focusRequester)
            .runIf(isComponentReady() && isSelectable) {
                pointerHoverIcon(PointerIcon.Text)
            }
            .runIf(isComponentReady()) {
                onDrag(
                    enabled = isSelectable,
                    onDragStart = {
                        log.v { "onDragStart ${it.x} ${it.y}" }
                        val transformedText = transformedTextRef.get() ?: return@onDrag
                        val isHoldingShiftKey = windowInfo.keyboardModifiers.isShiftPressed
                        draggedPoint = it
                        draggedPointAccumulated = it
                        dragStartPoint = it
                        dragStartViewportTop = viewportTop
                        dragStartViewportLeft = viewportLeft
                        if (!isHoldingShiftKey) {
                            val selectedCharIndex = getTransformedCharIndex(x = it.x, y = it.y, mode = ResolveCharPositionMode.Selection)
                                .let {
                                    log.d { "getTransformedCharIndex = $it" }
                                    viewState.roundedTransformedCursorIndex(it, CursorAdjustDirection.Bidirectional, transformedText, it, true)
                                }
                                .also { log.d { "onDragStart selected=$it" } }
                            viewState.transformedSelection = selectedCharIndex..selectedCharIndex
                            viewState.updateSelectionByTransformedSelection(transformedText)
                            viewState.transformedSelectionStart = selectedCharIndex
                        }
                        focusRequester.requestFocus()
//                    focusRequester.captureFocus()
                    },
                    onDrag = { // onDragStart happens before onDrag
                        log.v { "onDrag ${it.x} ${it.y}" }
                        draggedPoint += it
                        draggedPointAccumulated += it
                        log.v { "onDrag ${it.x}, ${it.y} -- ${draggedPoint.x}, ${draggedPoint.y} -- ${draggedPointAccumulated.x} ${draggedPointAccumulated.y}" }
                        continuousDragOffset = (draggedPoint - dragStartPoint) //* 0.25f
                        continuousDragLastMoveTime = KInstant.now()
                        onDragSelectAndScroll()
                    },
                    onDragEnd = {
                        println("onDragEnd")
                        dragOffset = Offset.Zero
                        dragStartPoint = Offset.Zero
                        continuousDragOffset = Offset.Zero
                        draggedPointAccumulated = Offset.Zero
                    },
                    onDragCancel = {
                        println("onDragCancel")
                    }
                )
                    .pointerInput(isEditable, isSelectable, weakRefOf(text), transformedText.hasLayouted, weakRefOf(viewState), viewportTop, viewportLeft, lineHeight, contentWidth, transformedText.length, transformedText.hashCode(), onPointerEvent) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val transformedText = transformedTextRef.get() ?: return@awaitPointerEventScope

                                if (onPointerEvent != null) {
                                    val position = event.changes.first().position
                                    val transformedCharIndex = getTransformedCharIndex(x = position.x, y = position.y, mode = ResolveCharPositionMode.Cursor)
                                    val tag = if (transformedCharIndex in 0 .. transformedText.lastIndex) {
                                        val charSequenceUnderPointer = transformedText.subSequence(transformedCharIndex, transformedCharIndex + 1)
                                        (charSequenceUnderPointer as? AnnotatedString)?.spanStyles?.firstOrNull { it.tag.isNotEmpty() }?.tag
                                    } else null
                                    onPointerEvent(event, tag)
                                }

                                when (event.type) {
                                    PointerEventType.Press -> {
                                        val position = event.changes.first().position
                                        val isHoldingShiftKey = windowInfo.keyboardModifiers.isShiftPressed
                                        log.v { "press ${position.x} ${position.y} shift=$isHoldingShiftKey" }

                                        if (event.button == PointerButton.Secondary) {
                                            isShowContextMenu = if (isSelectable) {
                                                !isShowContextMenu
                                            } else {
                                                false
                                            }
                                            continue
                                        }

                                        if (isHoldingShiftKey) {
                                            val selectionStart = viewState.transformedSelectionStart
                                            selectionEnd = getTransformedCharIndex(x = position.x, y = position.y, mode = ResolveCharPositionMode.Selection)
                                                .let {
                                                    log.d { "getTransformedCharIndex = $it" }
                                                    viewState.roundedTransformedCursorIndex(it, CursorAdjustDirection.Bidirectional, transformedText, it, true)
                                                }
                                            log.d { "shift press selectionStart = $selectionStart, selectionEnd = $selectionEnd" }
                                            viewState.transformedSelection = minOf(selectionStart, selectionEnd) until maxOf(selectionStart, selectionEnd)
                                            viewState.updateSelectionByTransformedSelection(transformedText)
                                        } else {
                                            viewState.transformedSelection = IntRange.EMPTY
                                            viewState.selection = EMPTY_SELECTION_RANGE
//                                    focusRequester.freeFocus()
                                        }

                                        viewState.transformedCursorIndex = getTransformedCharIndex(x = position.x, y = position.y, mode = ResolveCharPositionMode.Cursor)
                                        viewState.roundTransformedCursorIndex(CursorAdjustDirection.Bidirectional, transformedText, viewState.transformedCursorIndex, true)
                                        viewState.updateCursorIndexByTransformed(transformedText)
                                        if (!isHoldingShiftKey) {
                                            // for selection, max possible index is 1 less than that for cursor
                                            viewState.transformedSelectionStart = getTransformedCharIndex(x = position.x, y = position.y, mode = ResolveCharPositionMode.Selection)
                                        }
                                        log.v { "set cursor pos 1 => ${viewState.cursorIndex} t ${viewState.transformedCursorIndex}" }
                                        recordCursorXPosition()
                                        showCursor()
                                        focusRequester.requestFocus()
                                    }
                                }
                            }
                        }
                    }
                    .pointerInput(weakRefOf(transformedText), transformedText.hasLayouted, viewportTop, viewportLeft, lineHeight, contentWidth, weakRefOf(viewState), isSelectable) {
                        if (!isSelectable) return@pointerInput
                        detectTapGestures(onDoubleTap = {
                            val transformedText = transformedTextRef.get() ?: return@detectTapGestures

                            val wordStart = findPreviousWordBoundaryPositionFromCursor(isIncludeCursorPosition = true)
                            val wordEndExclusive = findNextWordBoundaryPositionFromCursor()
                            viewState.selection = wordStart until wordEndExclusive
                            viewState.updateTransformedSelectionBySelection(transformedText)
                            viewState.cursorIndex = wordEndExclusive
                            viewState.updateTransformedCursorIndexByOriginal(transformedText)
                            recordCursorXPosition()
                            showCursor()
                            focusRequester.requestFocus()
                        })
                    }
                    .onFocusChanged {
                        log.v { "BigMonospaceText onFocusChanged ${it.isFocused} ${it.hasFocus} ${it.isCaptured}" }
                        isFocused = it.isFocused
                        textInputSessionRef?.dispose()
                        if (isEditable) {
                            if (it.isFocused) {
                                val textInputSession = textInputService?.startInput(
                                    tv,
                                    ImeOptions.Default,
                                    { ed ->
                                        log.v { "onEditCommand [$ed] ${ed.joinToString { it::class.simpleName!! }} $tv" }
                                        ed.forEach {
                                            when (it) {
                                                is CommitTextCommand -> {
                                                    if (it.text.isNotEmpty()) {
                                                        onType(it.text)
                                                    }
                                                }
                                                is SetComposingTextCommand -> { // temporary text, e.g. SetComposingTextCommand(text='竹戈', newCursorPosition=1)
                                                    // TODO
                                                }
                                            }
                                        }
                                    },
                                    { a -> log.v { "onImeActionPerformed $a" } },
                                )
                                textInputSession?.notifyFocusedRect(
                                    Rect(
                                        layoutCoordinates!!.positionInRoot(),
                                        Size(
                                            layoutCoordinates!!.size.width.toFloat(),
                                            layoutCoordinates!!.size.height.toFloat()
                                        )
                                    )
                                )
                                if (textInputSession != null) {
                                    textInputSessionRef = textInputSession
                                    log.v { "started text input session" }
                                }
                                showCursor()
//                        keyboardController?.show()
                            } else {
//                        keyboardController?.hide()
                            }
                        }
                    }
                    .onPreviewKeyEvent {
                        log.v { "BigMonospaceText onPreviewKeyEvent ${it.type} ${it.key} ${it.key.nativeKeyCode} ${it.key.keyCode}" }
                        onProcessKeyboardInput(it)
                    }
            }
//            .then(BigTextInputModifierElement(1))
            // focusable cannot be always true, otherwise it would crash due to this bug: https://issuetracker.google.com/issues/274655703
            .focusable(isComponentReady() && isSelectable) // `focusable` should be after callback modifiers that use focus (https://issuetracker.google.com/issues/186567354)
            .semantics {
                log.w { "semantic lambda" }
                val text = textRef.get() ?: return@semantics
                val transformedText = transformedTextRef.get() ?: return@semantics
                if (isEditable) {
                    editableText = AnnotatedString(transformedText.buildString())
                    setText {
                        viewState.selection = 0 .. text.lastIndex
                        onType(it.text)
                        true
                    }
                    insertTextAtCursor {
                        onType(it.text)
                        true
                    }
                } else {
                    this.text = AnnotatedString(transformedText.buildString())
                    setText { false }
                    insertTextAtCursor { false }
                }
            }
//            .background(Color.Red)
            .run { // sizing canvas
//                with(density) {
//                    if (isSingleLineInput) {
//                        requiredHeight(lineHeight.toDp())
//                    } else {
//                        this@run
//                    }
//                        .forceHeightAtLeast(minHeight = lineHeight.roundToInt())
//                        .defaultMinSize(minWidth = 100.dp)
//                }

                val minWidthIfUnbounded = with(density) {
                    100.dp.roundToPx()
                }

                defaultMinSize(minWidth = 100.dp)
                    .layout { measurable, constraints ->
                        val lineHeightInt = lineHeight.roundToInt()
                        var minH = constraints.minHeight
                        var maxH = constraints.maxHeight
                        var minW = constraints.minWidth
                        var maxW = constraints.maxWidth
                        if (isSingleLineInput) {
                            minH = lineHeightInt
                            maxH = lineHeightInt
                        } else if (constraints.hasBoundedHeight) {
                            maxH = maxH.coerceAtLeast(lineHeightInt)
                            minH = maxH
                        } else { // unbounded
                            val transformedText = transformedTextRef.get()
                            if (transformedText == null) {
                                maxH = lineHeightInt
                            } else {
                                maxH = (transformedText.numOfRows * lineHeight).toInt().coerceAtLeast(lineHeightInt)
                            }
                            minH = maxH
                        }
                        if (!constraints.hasBoundedWidth) {
                            maxW = (transformedText.maxLineWidth.toFloat() / transformedText.widthMultiplier).roundToInt()
                                .coerceAtLeast(minWidthIfUnbounded)
                            minW = maxW
                        } else if (minW == 0) { // unspecified
//                            maxW = minWidthIfUnbounded
                        } else {
//                            minW = maxW
                        }
                        val placeable = measurable.measure(Constraints(
                            minWidth = minW, maxWidth = maxW, minHeight = minH, maxHeight = maxH
                        ))
//                        println("tf measure box cw=$minW..$maxW ch=$minH..$maxH => ${placeable.width} * ${placeable.height}")
                        layout(placeable.width.coerceIn(minW, maxW), placeable.height.coerceIn(minH, maxH)) {
                            placeable.place(0, 0)
                        }
                    }
//                    .background(Color.Blue)
            }

    ) {
        if (!isComponentReady()) {
            log.v { "tf isComponentReady=false '${text.buildString().abbr()}' return box" }
            return@Box
        }

        val transformedText = transformedTextRef.get() ?: run {
            log.v { "tf without transformedTextRef return box" }
            return@Box
        }
        val viewportBottom = viewportTop + height
        if (lineHeight > 0 && transformedText.hasLayouted) {

            fun drawInnerUIStart() = Unit

//            val firstRowIndex = maxOf(0, (viewportTop / lineHeight).toInt())
//            val lastRowIndex = minOf(transformedText.lastRowIndex, (viewportBottom / lineHeight).toInt() + 1)
            val rowRange = viewState.calculateVisibleRowRange(viewportTop.toInt())
            val firstRowIndex = rowRange.first
            val lastRowIndex = rowRange.last
            val numLines = transformedText.numOfLines
            log.v { "row index = [$firstRowIndex, $lastRowIndex]; scroll = $viewportTop ~ $viewportBottom; line h = $lineHeight" }
            viewState.firstVisibleRow = firstRowIndex
            viewState.lastVisibleRow = lastRowIndex

            val startInstant = KInstant.now()

            Canvas(modifier = Modifier.matchParentSize()) {
                val transformedText = transformedTextRef.get() ?: run {
                    log.v { "tf without transformedTextRef return canvas" }
                    return@Canvas
                }

                (firstRowIndex..lastRowIndex).forEach { i ->
                    val lineIndex = transformedText.findOriginalLineIndexByRowIndex(i)
                    val rowStartIndex = transformedText.findRowPositionStartIndexByRowIndex(i)
                    val rowEndIndex = if (i + 1 > transformedText.lastRowIndex) {
                        transformedText.length
                    } else {
                        transformedText.findRowPositionStartIndexByRowIndex(i + 1)
                    }
//                    val linePositionStartIndex = transformedText.findPositionStartOfLine(lineIndex)
//                    val rowPositionOffset = rowStartIndex - linePositionStartIndex
                    val renderStartIndex: Int
                    val renderEndIndexExclusive: Int
                    val yOffset: Dp = (-viewportTop + (i/* - firstRowIndex*/) * lineHeight).toDp()
                    val xOffset: Dp

//                    log.v { "row #$i line #$lineIndex s=$rowStartIndex e=$rowEndIndex ls=$linePositionStartIndex ro=$rowPositionOffset" }
                    log.v { "row #$i line #$lineIndex s=$rowStartIndex e=$rowEndIndex" }
                    log.v { "row #$i line #$lineIndex y=$yOffset viewportTop=$viewportTop lineHeight=$lineHeight" }

                    if (isSoftWrapEnabled) {
                        renderStartIndex = rowStartIndex
                        renderEndIndexExclusive = (rowEndIndex - if (rowEndIndex in (1 .. transformedText.length) && transformedText.substring(rowEndIndex - 1 ..< rowEndIndex).string() == "\n") {
                            1
                        } else {
                            0
                        }).coerceIn(
                            minimumValue = renderStartIndex,
                            maximumValue = maxOf(renderStartIndex, rowEndIndex)
                        )
                        xOffset = 0.dp
                    } else {
                        renderStartIndex = transformedText.findMaxEndPositionOfWidthSumOverPositionRangeAtMost(
                            startPosition = rowStartIndex,
                            endPositions = rowStartIndex .. maxOf(rowStartIndex, rowEndIndex - 1),
                            isEndExclusive = false,
                            maxWidthSum = viewportLeft.toInt(),
                        )
                        renderEndIndexExclusive = transformedText.findMinEndPositionOfWidthSumOverPositionRangeAtLeast(
                            startPosition = rowStartIndex,
                            endPositions = rowStartIndex + 1 .. rowEndIndex,
                            isEndExclusive = true,
                            minWidthSum = viewportLeft.toInt() + width,
                        ).coerceIn(
                            minimumValue = renderStartIndex,
                            maximumValue = (rowEndIndex - if (i < numLines - 1) 1 /* exclude the '\n' char */ else 0)
                                .coerceIn(renderStartIndex..transformedText.length)
                        )

                        xOffset = (-viewportLeft + transformedText.findWidthByPositionRangeOfSameLine(rowStartIndex ..< renderStartIndex).toInt()).toDp()
                    }
//                    log.v { "row #$i line #$lineIndex s=$rowStartIndex e=$rowEndIndex rs=$renderStartIndex re=$renderEndIndexExclusive ls=$linePositionStartIndex ro=$rowPositionOffset" }
                    log.v { "row #$i line #$lineIndex s=$rowStartIndex e=$rowEndIndex rs=$renderStartIndex re=$renderEndIndexExclusive" }

                    if (viewState.hasSelection()) {
                        val intersection = viewState.transformedSelection intersect (renderStartIndex .. renderEndIndexExclusive)
                        if (!intersection.isEmpty()) {
                            log.v { "row #$i - intersection: $intersection" }
                            drawRect(
                                color = textSelectionColors.backgroundColor,
                                topLeft = Offset(
                                    xOffset.toPx() + getTransformedStringWidth(renderStartIndex, intersection.start),
                                    yOffset.toPx()
                                ),
                                size = Size(
                                    getTransformedStringWidth(
                                        intersection.start,
                                        intersection.endInclusive + 1
                                    ),
                                    lineHeight,
                                ),
                            )
//                            Box(
//                                Modifier
//                                    .height(lineHeight.toDp())
//                                    .width(
//                                        getTransformedStringWidth(
//                                            intersection.start,
//                                            intersection.endInclusive + 1
//                                        ).toDp()
//                                    )
//                                    .offset(
//                                        x = xOffset + getTransformedStringWidth(renderStartIndex, intersection.start).toDp(),
//                                        y = yOffset
//                                    )
//                                    .background(color = textSelectionColors.backgroundColor) // `background` modifier must be after `offset` in order to take effect
//                            )
                        }
                    }

                    val rowText = transformedText.subSequence(
                        startIndex = renderStartIndex,
                        endIndex = renderEndIndexExclusive,
                    )

                    /**
                     * Draw text char by char rather than whole string or use BasicText(...) or Text(...),
                     * otherwise the char widths of the text drawn is not consistent with TextMeasurer for non-monospace fonts.
                     */
                    var accumulateXOffset = 0f
                    val rowAnnotatedString = rowText.annotatedString()
                    log.v { "draw line #$lineIndex row #$i char 0 .. ${rowAnnotatedString.lastIndex}" }
                    var surrogatePairFirstChar: Char? = null
                    (0 .. rowAnnotatedString.lastIndex).forEach { j ->
                        val charAnnotated = rowAnnotatedString.subSequence(j, j + 1)
                        if (charAnnotated.first().isSurrogatePairFirst()) {
                            surrogatePairFirstChar = charAnnotated.first()
                            return@forEach
                        }

                        val annotatedUnicode = if (surrogatePairFirstChar == null) {
                            charAnnotated
                        } else {
                            AnnotatedString(
                                text = "${surrogatePairFirstChar}${charAnnotated.text}",
                                spanStyles = charAnnotated.spanStyles
                                    .map { it.copy(start = 0, end = 2) }
                            )
                        }
//                        val charStyle = annotatedUnicode.spanStyles.map { it.item }.reduceOrNull { acc, it -> acc + it }?.let { textStyle + it }
                        val charWidth = textLayouter.measureCharWidth(annotatedUnicode)
                        val charYOffset = textLayouter.measureCharYOffset(annotatedUnicode)
//                        log.v { "char '$annotatedUnicode' w=$charWidth y=$charYOffset lh=$lineHeight" }
                        drawText(
                            textMeasurer = textMeasurer,
                            text = annotatedUnicode,
                            style = textStyle,
                            softWrap = false,
                            topLeft = Offset(xOffset.toPx() + accumulateXOffset, yOffset.toPx() + charYOffset),
                            size = Size(charWidth, lineHeight),
                        )
                        surrogatePairFirstChar = null
                        accumulateXOffset += charWidth
                    }

//                    drawText(
//                        textMeasurer = textMeasurer,
//                        text = rowText.annotatedString(),
//                        style = textStyle,
//                        softWrap = false,
//                        topLeft = Offset(xOffset.toPx(), yOffset.toPx()),
//                        size = Size(width - (padding.calculateStartPadding(LayoutDirection.Ltr) + padding.calculateEndPadding(LayoutDirection.Ltr)).toPx(), lineHeight),
//                    )

//                    BasicText(
//                        text = rowText.annotatedString(),
//                        style = textStyle,
//                        maxLines = 1,
//                        softWrap = false,
//                        modifier = Modifier.offset(y = yOffset, x = xOffset)
//                    )

                    log.v { "line = $i, cursor T = ${viewState.transformedCursorIndex} / O = ${viewState.cursorIndex}" }
                    if (isEditable && isFocused && viewState.transformedCursorIndex in renderStartIndex .. renderEndIndexExclusive && isCursorVisible) {
                        val x = if (viewState.transformedCursorIndex - renderStartIndex > 0) {
                            transformedText.findWidthByPositionRangeOfSameLine(
                                renderStartIndex..< viewState.transformedCursorIndex
                            ).also {
                                log.v { "cursor x find w = $it" }
                            }
                        } else {
                            0f
                        }.toDp() + xOffset
                        log.v { "cursor x = $x" }
                        drawRect(
                            color = cursorColor,
                            topLeft = Offset(x.toPx(), yOffset.toPx()),
                            size = Size(2.dp.toPx(), lineHeight),
                        )
//                        BigTextFieldCursor(
//                            lineHeight = lineHeight.toDp(),
//                            color = cursorColor,
//                            modifier = Modifier.offset(
//                                x = x,
//                                y = yOffset,
//                            )
//                        )
                    }
                }
            }

            val endInstant = KInstant.now()
            log.d { "Declare BigText content for render took ${endInstant - startInstant}" }
        } else {
            log.v { "tf else lineHeight=$lineHeight && transformedText.hasLayouted=${transformedText.hasLayouted}" }
        }

        if (isSelectable) {
            contextMenu(
                isShowContextMenu,
                { isShowContextMenu = false },
                listOfNotNull(
                    ContextMenuItemButton("Copy", viewState.hasSelection(), buildTestTag("Copy")!!) { copySelection() },
                    if (isEditable) ContextMenuItemButton("Paste", clipboardManager.hasText(), buildTestTag("Paste")!!) { paste() } else null,
                    if (isEditable) ContextMenuItemButton("Cut", viewState.hasSelection(), buildTestTag("Cut")!!) { cutSelection() } else null,
                    ContextMenuItemDivider(),
                    if (isEditable) ContextMenuItemButton("Undo", text.isUndoable(), buildTestTag("Undo")!!) { undo() } else null,
                    if (isEditable) ContextMenuItemButton("Redo", text.isRedoable(), buildTestTag("Redo")!!) { redo() } else null,
                    if (isEditable) ContextMenuItemDivider() else null,
                    ContextMenuItemButton("Select All", text.isNotEmpty, buildTestTag("Select All")!!) { selectAll() },
                ),
                "",
            )
        }
    }

    LaunchedEffect(Unit) {
        (cursorShowTrigger
            .receiveAsFlow()
            .flatMapLatest {
                flow {
                    emit(true)
                    while (coroutineContext.isActive) {
                        delay(700.milliseconds().millis)
                        emit(!isCursorVisible)
                    }
                }
            } as Flow<Boolean>) // IntelliJ IDEA won't work without this redundant cast
            .onEach {
                withContext(Dispatchers.Default) {
                    isCursorVisible = it
//                    log.v { "isCursorVisible changed to : $isCursorVisible" }
                }
            }
            .launchIn(this)

        cursorShowTrigger.send(Unit) // activate the flow
    }

    LaunchedEffect(transformedTextRef, continuousDragOffset.x * continuousDragOffset.y != 0f) {
//        return@LaunchedEffect
        if (continuousDragOffset.x != 0f || continuousDragOffset.y != 0f) {
            while (coroutineContext.isActive) {
                if ((continuousDragOffset.x != 0f || continuousDragOffset.y != 0f) && (KInstant.now() - continuousDragLastMoveTime) >= 300.milliseconds()) {
                    val it = continuousDragOffset
                    log.v { "cont drag ${it.x} ${it.y} -- ${draggedPointAccumulated.x} ${draggedPointAccumulated.y}" }
                    val transformedText = transformedTextRef.get() ?: return@LaunchedEffect
                    if (transformedText.isEmpty) {
                        viewState.transformedSelection = IntRange.EMPTY
                        viewState.selection = EMPTY_SELECTION_RANGE
                        viewState.transformedCursorIndex = 0
                        viewState.cursorIndex = 0
                        return@LaunchedEffect
                    }
//                    draggedPointAccumulated += it
//                    viewportTop += it.y
                    scrollState.scrollBy(it.y) // note that viewportTop is updated in the next frame
//                    viewportLeft += it.x
                    horizontalScrollState.scrollBy(it.x) // note that viewportLeft is updated in the next frame
                    onDragSelectAndScroll(it) // include the offset into calculation for more accurate cursor positions
                }
                delay(100L)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textInputSessionUpdatedRef.get()?.dispose()
            log.d { "BigTextField onDispose -- disposed input session" }

            (heavyJobScope.coroutineContext as? CloseableCoroutineDispatcher)?.close()
            log.d { "BigTextField onDispose -- closed coroutineContext" }
        }
    }

    DisposableEffect(weakRefOf(text), textManipulateListener) {
        onDispose {
            textManipulateListener?.let {
                text.unregisterCallback(it)
            }

            val transformedText = transformedTextRef.get()
            transformedText?.unbindChangeHook()

            log.d { "BigTextField onDispose -- disposed text manipulate listener and change hook" }
        }
    }

    fun endOfFunction() = Unit
}

private enum class ResolveCharPositionMode {
    Selection, Cursor
}

enum class TextFBDirection {
    Forward, Backward
}

enum class CursorAdjustDirection {
    Forward, Backward, Bidirectional
}

private enum class ContextMenuItem {
    Copy, Paste, Cut, Undo, Redo, SelectAll
}
