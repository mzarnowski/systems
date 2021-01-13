package dev.mzarnowski.system.pipeline;

import java.util.function.Function;

final class FlowMapped<A, B, C> extends FlowDownstream<A, B, C> {
    private final Function<B, C> f;

    FlowMapped(Flow<A, B> source, Function<B, C> f) {
        super(source);
        this.f = f;
    }


    @Override
    protected FlowStaged<B> staged(FlowStaged<C> next) {
        return new FlowStaged<>() {
            @Override
            public void accept(B value) {
                next.accept(f.apply(value));
            }

            @Override
            public void commit() {
                next.commit();
            }
        };
    }
}
