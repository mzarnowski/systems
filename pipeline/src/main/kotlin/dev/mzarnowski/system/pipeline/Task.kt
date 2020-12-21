package dev.mzarnowski.system.pipeline

import dev.mzarnowski.Disposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ScheduledExecutorService as Scheduler

abstract class Task(private val scheduler: Scheduler) : Disposable {
    private var requests = AtomicInteger(0)

    protected abstract fun iterate(): Result

    operator fun invoke() {
        if (0 == requests.getAndUpdate { if (it < 0) it else it + 1 }) {
            scheduler.execute(this::run)
        }
    }

    private fun run() {
        val batch = requests.get()

        when (val result = iterate()) {
            Break -> dispose()
            Continue -> requests.addAndGet(-batch).also { invoke() }
            is Delay -> scheduler.schedule(this::run, result.by, result.unit)
            Wait -> requests.addAndGet(-batch).also { if (it > 0) scheduler.execute(this::run) }
        }
    }

    override fun dispose() = requests.set(-1)

    abstract class Result internal constructor()

    // continue immediately, most often used by pumps driving the processing (neither observers nor adapters)
    object Continue : Result()

    // wait until another request comes, or continue immediately if one came during the iteration
    // most often used by pumps which are not driving the processing of the data
    object Wait : Result()

    // stop iteration altogether, triggering disposal of resources and preventing subsequent iterations
    object Break : Result()

    // reschedule current iteration
    data class Delay(val by: Long, val unit: TimeUnit) : Result()

    companion object {
        operator fun invoke(scheduler: Scheduler, iterate: () -> Result): Task {
            return object : Task(scheduler) {
                override fun iterate(): Result = iterate()
            }
        }

        operator fun invoke(scheduler: Scheduler, disposable: Disposable, iterate: () -> Result): Task {
            return object : Task(scheduler) {
                override fun iterate(): Result = iterate()
                override fun dispose() {
                    super.dispose()
                    disposable.dispose()
                }
            }
        }
    }
}
