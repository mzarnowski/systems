package dev.mzarnowski.system.pipeline

abstract class Reader<A> : Segment<A>() {
    fun read(at: Int): A = buffer[this.at + at]
}