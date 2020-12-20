package dev.mzarnowski.system.pipeline

open class Buffer<A>(capacity: Int) {
    val size: Int = capacity.nextPowerOfTwo()
    val mask: Int = this.size - 1

    @Suppress("UNCHECKED_CAST")
    val values: Array<A> = arrayOfNulls<Any>(size) as Array<A>

    operator fun get(at: Int): A = values[at and mask]
    operator fun set(at: Int, value: A) {
        values[at and mask] = value
    }
}

private fun Int.nextPowerOfTwo(): Int {
    if (countOneBits() == 1) return this
    return takeHighestOneBit() * 2
}