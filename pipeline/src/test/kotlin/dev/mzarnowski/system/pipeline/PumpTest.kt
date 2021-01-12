package dev.mzarnowski.system.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PumpTest {
    @Test
    fun read_all_values() {
        val scheduler = Scheduler(singleThreaded(), 1)

        val pipeline = Pipeline(scheduler, 32, 20)
        val pump = pipeline.stream(1..10000)

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.first()).isEqualTo(1)
            assertThat(actual.last()).isEqualTo(10000)
        }
    }

    @Test
    fun map_all_values() {
        val scheduler = Scheduler(singleThreaded(), 1)

        val pipeline = Pipeline(scheduler, 32, 20)
        val pump = pipeline.stream(1..1000).map {it + 3}

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.first()).isEqualTo(1 + 3)
            assertThat(actual.last()).isEqualTo(1000 + 3)
        }
    }

    private fun singleThreaded() = Executors.newSingleThreadScheduledExecutor { Thread(it, "foo") }
}


