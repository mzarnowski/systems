package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.function.Consumer;

public interface Pipe<A> extends Disposable {
    Disposable forEach(Consumer<A> f);
    boolean isDisposed();
}
