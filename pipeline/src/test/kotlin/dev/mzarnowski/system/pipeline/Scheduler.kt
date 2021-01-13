package dev.mzarnowski.system.pipeline

import org.junit.jupiter.api.fail
import java.util.concurrent.*

class Scheduler(threads: Int, n: Int) : ScheduledExecutorService by scheduler(threads) {
    private val counter = CountDownLatch(n)
    fun countDown() = counter.countDown()

    fun onceCompleted(f: () -> Unit) {
        val finished = counter.await(10, TimeUnit.SECONDS)
        if (finished) {
            shutdown()
            awaitTermination(5, TimeUnit.SECONDS)
            f()
        } else {
            val tasks = shutdownNow().size
            fail(TimeoutException("Missing ${counter.count} reports with $tasks stale tasks"))
        }
    }
}

private fun scheduler(threads: Int = 1) = when (threads) {
    1 -> Executors.newSingleThreadScheduledExecutor { Thread(it, "foo") }
    else -> Executors.newScheduledThreadPool(threads) { Thread(it, "foo") }
}