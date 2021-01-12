package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

final class Reader<A> implements Claimable, Disposable, Upstream.Of<A> {
    volatile int at = 0;
    volatile int available = 0;
    private final Buffer<A> buffer;
    private final Ring<A> ring;

    Reader(Buffer<A> buffer) {
        this.buffer = buffer;
        this.ring = buffer.ring;
    }

    @Override
    public void dispose() {
        buffer.unsubscribe(this);
    }

    @Override
    public void request() {
        buffer.request();
    }

    @Override
    public int claim(int atLeast, int atMost) {
        if (available < atLeast) {
            available = buffer.at - this.at;
        }

        return available < atLeast ? 0 : Math.min(available, atMost);
    }

    @Override
    public void release(int amount) {
        available -= amount;
        at += amount;
    }

    @Override
    public A read(int offset) {
        return ring.get(at + offset);
    }
}
