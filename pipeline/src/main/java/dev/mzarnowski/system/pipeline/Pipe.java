package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

interface Pipe<A> {
    Sink forEach(Consumer<A> consumer);
    <B> Pipe<B> map(Function<A, B> f);
    Pipe<A> filter(Predicate<A> predicate);
}
