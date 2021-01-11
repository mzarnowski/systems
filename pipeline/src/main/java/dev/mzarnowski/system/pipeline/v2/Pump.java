package dev.mzarnowski.system.pipeline.v2;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

final class Pump implements Disposable {
    static final int IDLE = 0;
    static final int RUNNING = 1;
    static final int QUEUED = 1;
    static final int COMPLETING = 3;
    static final int DISPOSED = 4;

    private final Executor executor;
    private final Runnable task;
    private final Runnable onComplete;
    private final Runnable onDispose;

    private final AtomicInteger state = new AtomicInteger(IDLE);

    Pump(Executor executor, Runnable task, Runnable onComplete, Runnable onDispose) {
        this.executor = executor;
        this.task = task;
        this.onComplete = onComplete;
        this.onDispose = onDispose;
    }

    Pump(Executor executor, Runnable task, Runnable onDispose) {
        this.executor = executor;
        this.task = task;
        this.onComplete = this::dispose;
        this.onDispose = onDispose;
    }

    void schedule() {
        if (IDLE == state.getAndUpdate(it -> it < QUEUED ? it + 1 : it)) {
            executor.execute(this::iterate);
        }
    }

    void complete() {
        if (IDLE == state.getAndUpdate(it -> Math.max(COMPLETING, it))) {
            executor.execute(onComplete);
        }
    }

    @Override
    public void dispose() {
        if (IDLE == state.getAndSet(DISPOSED)) {
            executor.execute(onDispose);
        }
    }

    private void iterate() {
        task.run();

        var is = state.updateAndGet(it -> it < COMPLETING ? it - 1 : it);
        if (is == RUNNING) executor.execute(this::iterate);
        else if (is == COMPLETING) executor.execute(onComplete);
        else if (is == DISPOSED) executor.execute(onDispose);
    }
}
