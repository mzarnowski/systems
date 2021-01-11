package dev.mzarnowski.system.pipeline.v2;

import java.util.HashSet;
import java.util.Set;

// cannot implement downstream, as sources are buffers but are not "downstream"
abstract class Buffer<A> implements Claimable {
    private final Ring<A> ring = Ring.of(64);
    private final Set<Reader<A>> downstream = new HashSet<>();

    public void write(int offset, A value) {
        // TODO ring.set(at + offset, value);
    }

    @Override
    public int claim(int amount) {
        return 0; // TODO
    }
}

final class Ring<A> {
    final int size;
    final int mask;
    final A[] values;

    static <A> Ring<A> of(int size) {
        return new Ring<>(nextPowerOfTwo(size));
    }

    private Ring(int size) {
        this.size = size;
        this.mask = size - 1;
        this.values = (A[]) new Object[size];
    }

    A get(int offset) {
        return values[offset & mask];
    }

    void set(int offset, A value) {
        values[offset & mask] = value;
    }

    private static int nextPowerOfTwo(int size) {
        if (Integer.bitCount(size) == 1) return size;
        return Integer.highestOneBit(size) * 2;
    }
}