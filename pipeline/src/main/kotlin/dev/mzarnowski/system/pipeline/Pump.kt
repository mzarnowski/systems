package dev.mzarnowski.system.pipeline

import dev.mzarnowski.Disposable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import java.util.concurrent.ScheduledExecutorService as Scheduler

internal class Pump<A>(private val scheduler: Scheduler, f: (Writer<A>) -> () -> Task.Result) : Disposable,
    Buffer<A>(32) {
    internal val disposed = AtomicBoolean(false)

    internal val writer: Upstream<A> = Upstream(this)
    private val pull: Task = Task(scheduler, this, f(writer)) // TODO make lateinit settable after construction
    private val downstream = mutableMapOf<Reader<A>, Task>()

    internal fun add(reader: Reader<A>, task: Task) {
        downstream[reader] = task
        while (true) {
            val at = (writer.at + writer.available) and mask
            if (reader.at == at) return
            reader.at = at
        }
    }

    fun forEach(f: (A) -> Unit): Disposable {
        val reader = Downstream(this)
        val task = ForEach(scheduler, reader, f)

        add(reader, task)
        return task.also(Task::invoke)
    }

    class Upstream<A>(override val buffer: Pump<A>) : Writer<A>() {
        override fun claim(atLeast: Int, atMost: Int): Int {
            return super.claim(atLeast, atMost).also {
                if (it < atMost) request()
            }
        }

        override fun calculateAvailability(): Int {
            val downstream = buffer.downstream.keys

            if (downstream.isEmpty()) return available
            var available = buffer.size
            downstream.forEach { reader ->
                val distance = buffer.size - (this.at - reader.at)
                if (distance == 0) return 0
                available = min(available, distance)
            }
            return available
        }

        override fun request() = buffer.downstream.values.forEach { it() }
    }

    class Downstream<A>(override val buffer: Pump<A>) : Reader<A>() {
        override fun claim(atLeast: Int, atMost: Int): Int {
            return super.claim(atLeast, atMost).also {
                if (it < atMost) request()
            }
        }

        override fun calculateAvailability(): Int = (buffer.writer.at - this.at)
        override fun request() = buffer.pull()
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            writer.dispose()
            pull.dispose()
            downstream.keys.forEach(Disposable::dispose)
            println("disposed")
        }
    }
}