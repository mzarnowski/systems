package dev.mzarnowski.system.pipeline;

import java.util.Iterator;

final class SourceFromIterable<A> extends Source<A> {
    private final Iterator<A> iterator;

    SourceFromIterable(Pipeline owner, Iterable<A> iterable) {
        super(owner);
        iterator = iterable.iterator();
    }

    @Override
    protected void move(int amount) {
        var moved = 0;
        while (moved < amount && iterator.hasNext()) {
            var next = iterator.next();
            write(moved++, next);
        }
        release(moved);
        if (!iterator.hasNext()) dispose();
    }
}