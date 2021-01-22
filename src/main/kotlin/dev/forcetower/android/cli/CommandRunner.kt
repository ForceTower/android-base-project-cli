package dev.forcetower.android.cli

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CommandRunner {
    suspend fun exec(command: String) = suspendCancellableCoroutine<Int> { continuation ->
        ProcessBuilder()
        try {
            val process = Runtime.getRuntime().exec(command)
            process.inputStream.bufferedReader().readText()
            continuation.resume(process.waitFor())
        } catch (error: Throwable) {
            continuation.resumeWithException(error)
        }
    }
}