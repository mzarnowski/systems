package dev.mzarnowski.system.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PumpTest {
    @Test
    fun recalculates_availability_after_releases_from_both_ends() {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        val pump = Pump<Int>(scheduler) { { Task.Break } }

        val writer = pump.writer

        val reader = Pump.Downstream(pump)
        val task = Task(scheduler) { Task.Break }
        pump.add(reader, task)

        writer.release(writer.claim())

        assertThat(reader.at).isEqualTo(0)
        assertThat(reader.claim()).isEqualTo(pump.size)
        assertThat(writer.at).isEqualTo(pump.size)
        assertThat(writer.claim()).isEqualTo(0)

        reader.release(20)

        assertThat(writer.claim()).isEqualTo(20)
        assertThat(reader.claim()).isEqualTo(12)
    }

    @Test
    fun read_all_values() {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        val values = IterableSource(1..1000000)
        val pump = Pump(scheduler, values)

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add)
        while (!pump.disposed.get()) continue
        scheduler.shutdownNow()
        assertThat(actual).containsExactlyElementsOf(1..1000000)
    }
}


