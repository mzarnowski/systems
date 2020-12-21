package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.function.Consumer;

abstract class Pump<A> extends Task implements Pipe<A> {
    protected final Pipeline owner;

    Pump(Pipeline owner) {
        super(owner.scheduler);
        this.owner = owner;
    }

    abstract Reader<A> reader();
    abstract void register(Reader<A> reader, Task task);

    public final Disposable forEach(Consumer<A> f) {
        var reader = reader();
        register(reader, new ForEach<>(owner, reader, f));
        return reader;
    }
}

