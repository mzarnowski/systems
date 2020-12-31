package dev.mzarnowski.system.pipeline;

import java.util.HashMap;
import java.util.Map;

abstract class Buffer<A> extends Pump<A> implements Writer<A> {
    volatile int at = 0;
    volatile int available = 0;

    final Ring<A> ring = Ring.of(owner.bufferSize);
    private final Map<Reader<A>, Task> downstream = new HashMap<>();

    Buffer(Pipeline owner) {
        super(owner);
    }

    public final void set(int offset, A value) {
        ring.set(at + offset, value);
    }

    public final int claim() {
        return claim(1, Integer.MAX_VALUE);
    }

    public final int claim(int atLeast) {
        return claim(atLeast, Integer.MAX_VALUE);
    }

    public final int claim(int atLeast, int atMost) {
        if (available < atLeast) {
            if (downstream.isEmpty()) return available;
            var acc = ring.size;
            for (Reader<A> reader : downstream.keySet()) {
                var distance = ring.size - (this.at - reader.at);
                if (distance == 0) return 0;
                acc = Math.min(acc, distance);
            }
            available = acc;
        }

        if (available < atLeast) return 0;
        return Math.min(available, atMost);
    }

    public final void release(int amount) {
        available -= amount;
        at += amount;
        // TODO is it a good idea to always propagate here?
        request();
    }

    public final void request() {
        for (var pump : downstream.values()) pump.invoke();
    }

    @Override
    protected final void register(Reader<A> reader, Task task, boolean start) {
        if (isDisposed()) {
            task.dispose();
            return;
        }

        downstream.put(reader, task);

        // TODO this might need a temporary, guard Reader (always sitting at an arbitrary position)
        while (!isDisposed()) {
            var position = at + available;
            if (reader.at == position) break;
            reader.at = position;
        }

        if (isDisposed()) task.dispose();
        else if (start) task.invoke();
    }

    @Override
    Reader<A> reader() {
        return new Reader<>(this);
    }
}

