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
        val pump = pipeline.stream(1..1000).map { it + 3 }

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.first()).isEqualTo(1 + 3)
            assertThat(actual.last()).isEqualTo(1000 + 3)
        }
    }

    @Test
    fun filter_odd_values() {
        val scheduler = Scheduler(singleThreaded(), 1)
        val predicate = { i: Int -> i and 1 == 0 }
        val pipeline = Pipeline(scheduler, 32, 20)
        val pump = pipeline.stream(1..1000).filter(predicate)

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.size).isEqualTo(1000 / 2)
            assertThat(actual.filterNot(predicate)).isEmpty()
        }
    }

    @Test
    fun adapt_using_flow() {
        val scheduler = Scheduler(singleThreaded(), 1)

        val pipeline = Pipeline(scheduler, 32, 7)
        val pump = pipeline.stream(1..1000).adapt { it.map { n -> n * 4 }.filter { n -> n % 10 == 0 } }

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.size).isEqualTo(200)
            assertThat(actual.first()).isEqualTo(20)
            assertThat(actual.last()).isEqualTo(1000 * 4)
        }
    }

    @Test
    fun transfer_data_through_long_chain() {
        val scheduler = Scheduler(singleThreaded(), 1)
        val length = 100

        val pipeline = Pipeline(scheduler, 32, 7)
        var pump = pipeline.stream(1..1000)
        repeat(length) {
            pump = pump.map { it + 1 }
        }

        val actual = mutableListOf<Int>()
        pump.forEach(actual::add).onComplete(scheduler::countDown)

        scheduler.onceCompleted {
            assertThat(actual.first()).isEqualTo(1 + length)
            assertThat(actual.last()).isEqualTo(1000 + length)
        }
    }

    private fun singleThreaded() = Executors.newSingleThreadScheduledExecutor { Thread(it, "foo") }
}


