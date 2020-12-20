package dev.mzarnowski.system.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BufferTest {
    @Test
    fun buffer_size_is_the_power_of_two(){
        val buffer = Buffer<Int>(10)

        assertThat(buffer.size).isEqualTo(16)
    }

    @Test
    fun write_and_read_index_is_masked(){
        val buffer = Buffer<Int>(10)
        buffer[20] = 999

        assertThat(buffer[4]).isEqualTo(999)
    }
}