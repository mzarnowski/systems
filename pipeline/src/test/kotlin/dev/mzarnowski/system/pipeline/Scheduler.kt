package dev.mzarnowski.system.pipeline

import org.junit.jupiter.api.fail
import java.util.concurrent.*

class Scheduler(underlying: ScheduledExecutorService, n: Int) : ScheduledExecutorService by underlying {
    private val counter = CountDownLatch(n)
    fun countDown() = counter.countDown()

    fun onceCompleted(f: () -> Unit) {
        val finished = counter.await(5, TimeUnit.SECONDS)
        if (finished) {
            shutdown()
            awaitTermination(5, TimeUnit.SECONDS)
            f()
        } else {
            shutdownNow()
            fail(TimeoutException("Pipeline didn't complete its work: Missing ${counter.count} reports"))
        }
    }
}
