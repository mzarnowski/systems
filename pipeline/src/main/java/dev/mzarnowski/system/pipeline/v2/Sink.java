package dev.mzarnowski.system.pipeline.v2;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;

final class Sink<A> extends Reader<A> implements Disposable, Downstream {
    private final Runnable onComplete = this::dispose;
    private final Pump pump;
    private final Foo move;

    <B> Sink(Executor executor, Upstream upstream, B sink, Foo.Factory<Upstream.Of<A>, B> f) {
        super(upstream);
        this.pump = new Pump(executor, this::pump, this::drain, onComplete);
        this.move = f.apply(this, sink);
    }

    @Override
    public void onAvailable() {
        pump.schedule();
    }

    @Override
    public void dispose() {
        pump.dispose();
    }

    private void pump() {
        var available = claim(1);
        if (available > 0) move.move(available);
    }

    private void drain() {
        var available = claim(1);
        if (available == 0) dispose();
        else move.move(available);
    }
}
