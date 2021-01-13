package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

abstract class Sink<A> extends Pump implements Disposable, Downstream, Component {
    protected final Reader<A> upstream;

    Sink(Pipeline owner, Reader<A> upstream) {
        super(owner);
        this.upstream = upstream;
        onComplete(upstream::dispose);
    }

    protected abstract void move(int amount);

    @Override
    public void onAvailable() {
        schedule();
    }

    @Override
    public void onComplete() {
        complete();
    }

    @Override
    public Component onComplete(Runnable task) {
        onComplete.add(task);
        return this;
    }

    @Override
    public Component onError(ErrorHandler handler) {
        onError.add(handler);
        return this;
    }

    protected void pump() {
        var available = upstream.claim(1, batchSize);
        move(available);
    }

    protected void drain() {
        var available = upstream.claim(1, batchSize);
        if (available == 0) dispose();
        else move(available);
    }
}
