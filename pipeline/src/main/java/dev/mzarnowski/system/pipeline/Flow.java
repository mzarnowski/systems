package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class Flow<A, B> {
    public final <C> Flow<A, C> map(Function<B, C> f) {
        return new FlowMapped<>(this, f);
    }

    public final Flow<A, B> filter(Predicate<B> p) {
        return new FlowFiltered<>(this, p);
    }

    public final Terminal<A> forEach(Consumer<B> c) {
        return new FlowToConsumer<>(this, c);
    }

    /*internal*/
    final Terminal<A> forEach(Buffer<B> b) {
        return new FlowToBuffer<>(this, b);
    }

    abstract FlowStaged<A> build(FlowStaged<B> next);

    public static abstract class Terminal<A> {
        protected abstract FlowStaged<A> build();
    }
}


