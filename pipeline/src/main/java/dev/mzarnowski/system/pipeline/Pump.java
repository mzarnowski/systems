package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

// IDLE -> RUNNING(false/true) -> COMPLETING -> COMPLETED
abstract class Pump extends FeedbackComponent implements Disposable {
    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    //  private static final int QUEUED = 2;
    private static final int COMPLETING = 3;
    private static final int COMPLETED = 4;

    protected final Executor executor;
    protected final int batchSize;

    private final AtomicInteger state = new AtomicInteger(IDLE);

    Pump(Pipeline owner) {
        super(owner);
        this.executor = owner.scheduler;
        this.batchSize = owner.batchSize;
    }

    protected abstract void pump();
    protected abstract void drain();

    final void schedule() {
        if (IDLE == state.getAndUpdate(it -> it <= RUNNING ? it + 1 : it)) {
            executor.execute(this::iterate);
        }
    }

    final void complete() {
        if (IDLE == state.getAndUpdate(it -> Math.max(it, COMPLETING))) {
            executor.execute(this::iterate);
        }
    }

    @Override
    public final void dispose() {
        if (IDLE == state.getAndSet(COMPLETED)) {
            executor.execute(this::iterate);
        }
    }

    private void iterate() {
        var was = state.get();
        if (was == COMPLETED) {
            onComplete.run();
        } else if (was == COMPLETING) {
            call(this::drain);
            executor.execute(this::iterate);
        } else {
            call(this::pump);
            var is = state.updateAndGet(it -> it < COMPLETING ? it - 1 : it);
            if (is == IDLE) return;
            executor.execute(this::iterate);
        }
    }

    private void call(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            onError.forEach(handler -> handler.accept(this, e));
        }
    }
}