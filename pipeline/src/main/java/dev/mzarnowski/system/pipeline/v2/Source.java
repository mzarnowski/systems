package dev.mzarnowski.system.pipeline.v2;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;

final class Source<A> extends Buffer<A> implements Disposable, Upstream {
    private final Runnable onComplete = this::dispose;
    private final Pump pump;
    private final Foo move;

    <B> Source(Executor executor, B source, Foo.Factory<B, Buffer<A>> f) {
        this.pump = new Pump(executor, this::pump, onComplete);
        this.move = f.apply(source, this);
    }

    @Override
    public void request() {
        pump.schedule();
    }

    @Override
    public void dispose() {
        pump.dispose();
    }

    private void pump() {
        var capacity = claim(1);
        if (capacity > 0) move.move(capacity);
    }
}
