package dev.mzarnowski.system.pipeline;

import java.util.function.Predicate;

final class FlowFiltered<A, B> extends FlowDownstream<A, B, B> {
    private final Predicate<B> p;

    FlowFiltered(Flow<A, B> source, Predicate<B> p) {
        super(source);
        this.p = p;
    }

    @Override
    protected FlowStaged<B> staged(FlowStaged<B> next) {
        return new FlowStaged<>() {
            @Override
            public void accept(B value) {
                if (p.test(value)) next.accept(value);
            }

            @Override
            public void commit() {
                next.commit();
            }
        };
    }
}
