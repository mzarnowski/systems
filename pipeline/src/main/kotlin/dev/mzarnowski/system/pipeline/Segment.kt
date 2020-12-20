package dev.mzarnowski.system.pipeline

import dev.mzarnowski.Disposable

abstract class Segment<A> : Disposable {
    abstract val buffer: Buffer<A>

    @Volatile
    var at: Int = 0

    @Volatile
    var available: Int = 0

    @Volatile
    var disposed = false

    open fun claim(atLeast: Int = 1, atMost: Int = Int.MAX_VALUE): Int {
        if (available < atLeast) {
            available = calculateAvailability()
        }

        if (available >= atMost) return atMost

        return if (available < atLeast) 0 else available
    }

    fun release(amount: Int) {
        available -= amount
        at = (at + amount)
    }

    override fun dispose() {
        disposed = true
    }

    protected abstract fun calculateAvailability(): Int
    abstract fun request()
}