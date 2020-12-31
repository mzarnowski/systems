package dev.mzarnowski.system.pipeline;

public final class Reader<A> implements Rationed {
    volatile int at = 0;
    volatile int available = 0;

    private final Buffer<A> buffer;
    private final Ring<A> ring;

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

        return Math.min(available, atMost);
    }

    public void release(int amount) {
        available -= amount;
        at += amount;
    }

    public boolean request() {
        if (buffer.isDisposed())
            return false;
        buffer.invoke();
        return true;
    }

    public A get(int offset) {
        return ring.get(at + offset);
    }
}