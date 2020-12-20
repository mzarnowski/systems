package dev.mzarnowski.system.pipeline

import java.util.concurrent.ScheduledExecutorService as Scheduler

class ForEach<A>(scheduler: Scheduler, val reader: Reader<A>, val f: (A) -> Unit) : Task(scheduler) {
    override fun iterate(): Result {
        val available = reader.claim(1, 32)
        if (available < 0) return Break

        reader.request() // eagerly request
        if (available > 0) {
            repeat(available) {
                val next = reader.read(it)
                f(next)
            }

            reader.release(available)
        }

        return Continue
    }
}