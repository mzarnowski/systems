package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;

final class FlowToConsumer<A, B> extends Flow.Terminal<A> {
    private final Flow<A, B> source;
    private final Consumer<B> consumer;

    FlowToConsumer(Flow<A, B> source, Consumer<B> consumer) {
        this.source = source;
        this.consumer = consumer;
    }

    @Override
    public FlowStaged<A> build() {
        var staged = new FlowStaged<B>() {

            @Override
            public void accept(B value) {
                consumer.accept(value);
            }

            @Override
            public void commit() {
            }
        };

        return source.build(staged);
    }
}
