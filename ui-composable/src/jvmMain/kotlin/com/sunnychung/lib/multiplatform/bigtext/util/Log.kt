package com.sunnychung.lib.multiplatform.bigtext.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.MutableLoggerConfig
import co.touchlab.kermit.Severity
import com.sunnychung.lib.multiplatform.bigtext.util.JvmLogger

val log = Logger(object : MutableLoggerConfig {
    override var logWriterList: List<LogWriter> = listOf(JvmLogger())
    override var minSeverity: Severity = Severity.Warn
}, tag = "BigText.Other")
val llog = log
