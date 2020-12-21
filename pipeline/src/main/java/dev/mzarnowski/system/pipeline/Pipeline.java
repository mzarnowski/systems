package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.concurrent.ScheduledExecutorService;

public final class Pipeline implements Disposable {
    final ScheduledExecutorService scheduler;
    final int bufferSize;
    final int batchSize;

    Pipeline(ScheduledExecutorService scheduler, int bufferSize, int batchSize) {
        this.scheduler = scheduler;
        this.bufferSize = bufferSize;
        this.batchSize = batchSize;
    }

    public <A> Pipe<A> stream(Iterable<A> iterable) {
        return new FromIterable<>(this, iterable);
    }

    @Override
    public void dispose() {
        scheduler.shutdown(); // TODO handle stale tasks?
    }
}

