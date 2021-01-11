package dev.mzarnowski.system.pipeline.v2;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;

final class Adapter<A, B> extends Buffer<B> implements Disposable, Upstream, Downstream.Of<B> {
    private final Runnable onComplete = this::dispose;
    private final Reader<A> upstream;
    private final Pump pump;
    private final Foo move;

    private Adapter(Executor executor, Reader<A> upstream, Foo.Factory<Upstream.Of<A>, Downstream.Of<B>> f) {
        this.upstream = upstream;
        this.pump = new Pump(executor, this::pump, this::drain, onComplete);
        this.move = f.apply(upstream, this);
    }

    @Override
    public void onAvailable() {
        pump.schedule();
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
        var available = upstream.claim(32);
        if (available > 0) move.move(available);
    }

    private void drain() {
        var available = upstream.claim(32);
        if (available == 0) dispose();
        else move.move(available);
    }
}
