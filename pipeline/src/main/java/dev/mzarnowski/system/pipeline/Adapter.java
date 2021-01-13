package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

abstract class Adapter<A, B> extends Buffer<B> implements Disposable, Upstream, Downstream.Of<B> {
    protected final Reader<A> upstream;

    Adapter(Pipeline owner, Reader<A> upstream) {
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
    public void request() {
        schedule();
    }

    protected void pump() {
        var capacity = claim(1, batchSize);
        if (capacity == 0) return; // TODO check if we don't need to notify downstream
        var available = upstream.claim(1, batchSize);
        if (available == 0) upstream.request();
        else move(available);
    }

    protected void drain() {
        var capacity = claim(1, batchSize);
        if (capacity == 0) return; // TODO check if we don't need to notify downstream
        var available = upstream.claim(1, batchSize);
        if (available == 0) dispose();
        else move(available);
    }

    @Override
    public Pipe<B> onComplete(Runnable task) {
        onComplete.add(task);
        return this;
    }
}
