package dev.mzarnowski.system.pipeline;

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