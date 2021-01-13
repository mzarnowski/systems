package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

abstract class Source<A> extends Buffer<A> implements Disposable, Upstream {
    Source(Pipeline owner) {
        super(owner);
    }

    protected abstract void move(int amount);

    @Override
    public void request() {
        schedule();
    }

    protected void pump() {
        var capacity = claim(1, batchSize);
        if (capacity > 0) move(capacity);
    }

    @Override
    protected void drain() {
        dispose();
    }

    @Override
    public Source<A> onComplete(Runnable task) {
        onComplete.add(task);
        return this;
    }

    @Override
    public Pipe<A> onError(ErrorHandler handler) {
        onError.add(handler);
        return this;
    }
}
