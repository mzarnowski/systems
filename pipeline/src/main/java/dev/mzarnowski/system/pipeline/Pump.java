package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;

abstract class Pump<A> extends Task implements Pipe<A> {
    protected final Pipeline owner;
    private final Callback onComplete = new Callback(super::dispose);

    Pump(Pipeline owner) {
        super(owner.scheduler);
        this.owner = owner;
    }

    abstract Reader<A> reader();
    protected abstract void register(Reader<A> reader, Task task, boolean start);

    public final Component forEach(Consumer<A> f) {
        var reader = reader();
        var task = new ForEach<>(owner, reader, f);
        register(reader, task, true);
        return task;
    }

    @Override
    public void dispose() {
        onComplete.run();
    }

    @Override
    public Pipe<A> onComplete(Runnable runnable) {
        onComplete.add(runnable);
        return this;
    }
}

