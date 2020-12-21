package dev.mzarnowski.system.pipeline;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

final class FromIterable<A> extends Buffer<A> {
    private final Iterator<A> iterator;

    public FromIterable(Pipeline owner, Iterable<A> source) {
        super(owner);
        this.iterator = source.iterator();
    }

    @NotNull
    protected Result iterate() {
        var available = claim(1, owner.batchSize);
        if (available == 0 && isDisposed()) return Break.INSTANCE;
        // TODO writer.request(); ?

        var offset = 0;
        while (iterator.hasNext() && offset < available) {
            var next = iterator.next();
            set(offset++, next);
        }

        release(offset);
        return iterator.hasNext() ? Continue.INSTANCE : Break.INSTANCE;
    }
}