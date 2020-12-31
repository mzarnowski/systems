package dev.mzarnowski.system.pipeline;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Reader<A> implements Rationed {
    volatile int at = 0;
    volatile int available = 0;
    private final Buffer<A> buffer;
    private final Ring<A> ring;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    Reader(Buffer<A> buffer) {
        this.buffer = buffer;
        this.ring = buffer.ring;
    }

    public int claim() {
        return claim(1, Integer.MAX_VALUE);
    }

    public int claim(int atLeast) {
        return claim(atLeast, Integer.MAX_VALUE);
    }

    public int claim(int atLeast, int atMost) {
        if (available < atLeast) {
            available = buffer.at - this.at;
        }

        if (available == 0) return isDisposed() ? -1 : 0;
        if (available < atLeast) return isDisposed() ? available : 0;
        return Math.min(available, atMost);
    }

    public void release(int amount) {
        available -= amount;
        at += amount;
    }

    public void request() {
        buffer.invoke();
    }

    public A get(int offset) {
        return ring.get(at + offset);
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    public void dispose() {
        isDisposed.set(true);
        // TODO nothing more? Maybe stop the pump?
    }
}