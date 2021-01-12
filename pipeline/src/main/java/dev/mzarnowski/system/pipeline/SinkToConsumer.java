package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;

final class SinkToConsumer<A> extends Sink {
    private final Reader<A> upstream;
    private final Consumer<A> consumer;

    SinkToConsumer(Pipeline owner, Reader<A> upstream, Consumer<A> consumer) {
        super(owner, upstream);
        this.upstream = upstream;
        this.consumer = consumer;
        schedule();
    }

    @Override
    protected void move(int amount) {
        upstream.request();
        if (amount == 0) return;

        for (int i = 0; i < amount; i++) {
            var next = upstream.read(i);
            consumer.accept(next);
        }
        upstream.release(amount);
    }
}
