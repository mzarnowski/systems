package dev.mzarnowski.system.pipeline

abstract class Writer<A> : Segment<A>() {
    fun write(at: Int, value: A) {
        buffer[this.at + at] = value
    }
}