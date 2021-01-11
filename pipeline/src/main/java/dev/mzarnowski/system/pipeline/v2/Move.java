package dev.mzarnowski.system.pipeline.v2;

import java.util.function.BiFunction;

interface Foo {
    void move(int amount);

    interface Factory<A, B> extends BiFunction<A, B, Foo> {
    }
}

abstract class Move<From, To> implements Foo {
    protected final From upstream;
    protected final To downstream;

    protected Move(From upstream, To downstream) {
        this.upstream = upstream;
        this.downstream = downstream;
    }
}
