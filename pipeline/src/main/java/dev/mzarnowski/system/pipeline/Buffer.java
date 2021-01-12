package dev.mzarnowski.system.pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// cannot implement downstream, as sources are buffers but are not "downstream"
abstract class Buffer<A> extends Pump implements Claimable, Upstream, Pipe<A> {
    volatile int at = 0;
    volatile int available = 0;

    final Ring<A> ring;
    final Map<Reader<A>, Downstream> downstream = new ConcurrentHashMap<>();

    Buffer(Pipeline owner) {
        super(owner);
        this.ring = Ring.of(owner.bufferSize);
        onComplete.add(() -> downstream.values().forEach(Downstream::onComplete));
    }

    public final Sink forEach(Consumer<A> consumer) {
        return register(true, (reader) -> new SinkToConsumer<>(owner, reader, consumer));
    }

    @Override
    public final <B> Pipe<B> map(Function<A, B> f) {
        return register(false, (reader) -> new AdaptUsingFunction<>(owner, reader, f));
    }

    @Override
    public Pipe<A> filter(Predicate<A> p) {
        return register(false, (reader) -> new AdaptUsingPredicate<>(owner, reader, p));
    }

    public final void write(int offset, A value) {
        ring.set(at + offset, value);
    }

    @Override
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

        return available < atLeast ? 0 : Math.min(available, atMost);
    }

    public final void release(int amount) {
        available -= amount;
        at += amount;
        downstream.values().forEach(Downstream::onAvailable);
    }

    final <P extends Pump & Downstream> P register(boolean start, Function<Reader<A>, P> f) {
        var reader = new Reader<>(this);
        var pump = f.apply(reader);

        if (isDisposed()) {
            pump.dispose();
            return pump;
        }

        downstream.put(reader, pump);

        // TODO this might need a temporary, guard Reader (always sitting at an arbitrary position)
        while (!isDisposed()) {
            var position = at + available;
            if (reader.at == position) break;
            reader.at = position;
        }

        if (isDisposed()) pump.dispose();
        else if (reader.claim(1, 1) > 0) pump.schedule();
        else if (start) pump.schedule();

        return pump;
    }

    final void unsubscribe(Reader<A> reader) {
        downstream.remove(reader);
    }
}