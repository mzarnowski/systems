package dev.mzarnowski.system.pipeline;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

final class ForEach<A> extends Sink {
    private final Pipeline owner;
    private final Reader<A> reader;
    private final Consumer<A> consumer;

    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    public ForEach(Pipeline owner, Reader<A> reader, Consumer<A> consumer) {
        super(owner.scheduler);
        this.owner = owner;
        this.reader = reader;
        this.consumer = consumer;
    }

    @Override
    public void dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            super.dispose();
        }
    }

    @NotNull
    protected Result iterate() {
        // TODO create observer class, which is not requesting eagerly and is disposed automatically
        var requested = reader.request();

        var available = reader.claim(1, owner.batchSize);
        for (int i = 0; i < available; i++) {
            var next = reader.get(i);
            consumer.accept(next);
        }

        reader.release(available);
        if (requested) return Wait.INSTANCE;
        if (available == 0) return Break.INSTANCE;
        return Continue.INSTANCE;
    }
}