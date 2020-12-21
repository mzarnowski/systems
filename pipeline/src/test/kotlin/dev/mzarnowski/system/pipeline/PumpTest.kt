package dev.mzarnowski.system.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PumpTest {
    @Test
    fun recalculates_availability_after_releases_from_both_ends() {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        val pipeline = Pipeline(scheduler, 32, 8)
        val pipe = object : Buffer<Int>(pipeline) {
            override fun iterate(): Result = Break
        }

        val reader = Reader(pipe)
        val task = Task(scheduler) { Task.Break }
        pipe.register(reader, task)

        pipe.release(pipe.claim())

        assertThat(reader.at).isEqualTo(0)
        assertThat(reader.claim()).isEqualTo(32)
        assertThat(pipe.at).isEqualTo(32)
        assertThat(pipe.claim()).isEqualTo(0)

        reader.release(20)

        assertThat(pipe.claim()).isEqualTo(20)
        assertThat(reader.claim()).isEqualTo(12)
    }

    @Test
    fun read_all_values() {
        val scheduler = Executors.newSingleThreadScheduledExecutor { Thread(it, "foo")}
        val pipeline = Pipeline(scheduler, 32, 20)
        val pump = pipeline.stream(1..1000)

        val actual = mutableListOf<Int>()
        pump.forEach {
            if (it % 1000 == 0) println(it)
            actual.add(it)
        }
        while (!pump.isDisposed) continue
        pipeline.dispose()

        assertThat(actual.first()).isEqualTo(1)
        assertThat(actual.last()).isEqualTo(1000)
    }
}


