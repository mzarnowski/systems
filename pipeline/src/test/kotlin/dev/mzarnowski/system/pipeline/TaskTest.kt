package dev.mzarnowski.system.pipeline

import dev.mzarnowski.Disposable
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.LongAdder

class TaskTest {
    @Test
    fun does_not_start_automatically() {
        val adder = LongAdder()
        val scheduler = scheduler()
        val increment = Task(scheduler, scheduler.disposable()) {
            adder.increment()
            Task.Continue
        }

        Thread.sleep(300)
        scheduler.terminateOrFail()
    }

    @Test
    fun starts_when_requested() {
        val adder = LongAdder()
        val scheduler = scheduler()
        val increment = Task(scheduler, scheduler.disposable()) {
            adder.increment()
            Task.Continue
        }

        increment()

        scheduler.terminateOrFail()
        Assertions.assertThat(adder.sum()).isEqualTo(1L)
    }

    @Test
    fun runs_at_most_once_more_when_requested_during_execution() {
        val adder = LongAdder()
        val scheduler = scheduler()

        var increment: Task? = null
        increment = Task(scheduler, scheduler.disposable()) {
            if (adder.sum() == 0L) repeat(5) { increment!!.invoke() } //schedule another increment during execution
            adder.increment()

            if (adder.sum() > 1L) Task.Break
            else Task.Continue
        }

        increment()

        scheduler.terminateOrFail { adder.sum() >= 2 }
        Assertions.assertThat(adder.sum()).isEqualTo(2L)
    }

    @Test
    fun disposes_after_break() {
        val scheduler = scheduler()
        val task = Task(scheduler, scheduler.disposable()) { Task.Break }

        task()

        if (!scheduler.awaitTermination(5, SECONDS)) {
            fail("Scheduler $this was not shutdown")
        }
    }

    @Test
    fun ignores_subsequent_requests_after_break() {
        val adder = LongAdder()
        val scheduler = scheduler()
        var task: Task? = null
        task = Task(scheduler, scheduler.disposable()) {
            task!!.invoke()
            adder.increment()
            if (adder.sum() == 4L) Task.Break
            else Task.Continue
        }

        task()

        scheduler.terminateOrFail { adder.sum() >= 4 }
        Assertions.assertThat(adder.sum()).isEqualTo(4L)
    }

    @Test
    fun delays_execution() {
        val adder = LongAdder()
        val scheduler = scheduler()
        val timestamps = LongArray(2)

        val task = Task(scheduler, scheduler.disposable()) {
            timestamps[adder.sum().toInt()] = System.nanoTime()
            adder.increment()
            if (adder.sum() == 1L) Task.Delay(300, MILLISECONDS)
            else Task.Break
        }

        task()

        scheduler.terminateOrFail { adder.sum() >= 2 }
        Assertions.assertThat(timestamps[1] - timestamps[0])
            .isGreaterThanOrEqualTo(MILLISECONDS.toNanos(300))
    }
}

internal fun ExecutorService.disposable(): Disposable = object : Disposable {
    override fun dispose() = this@disposable.shutdown()
}

internal fun scheduler() = Executors.newScheduledThreadPool(4)
internal fun ExecutorService.terminateOrFail(seconds: Long = 5, f: () -> Boolean = { true }) {
    while (!f()) continue
    shutdown()
    if (!awaitTermination(seconds, SECONDS)) {
        val tasks = shutdownNow().size
        fail("Scheduler $this did not terminate within ${seconds}s leaving $tasks stale tasks")
    }
}