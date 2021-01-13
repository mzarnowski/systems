package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

abstract class Pump extends FeedbackComponent implements Disposable {
    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int QUEUED = 2;
    private static final int COMPLETED = 3;

    protected final Executor executor;
    protected final int batchSize;
    private final Runnable initialTask = this::pump;
    private final AtomicInteger state = new AtomicInteger(IDLE);
    private final AtomicReference<Runnable> task = new AtomicReference<>(initialTask);

    Pump(Pipeline owner) {
        super(owner);
        this.executor = owner.scheduler;
        this.batchSize = owner.batchSize;
        onComplete(() -> state.set(COMPLETED));
    }

    protected abstract void pump();
    protected abstract void drain();

    final void schedule() {
        if (IDLE == state.getAndUpdate(it -> it < QUEUED ? it + 1 : it)) {
            executor.execute(this::iterate);
        }
    }

    final void complete() {
        if (task.compareAndSet(initialTask, this::drain)) {
            schedule();
        }
    }

    @Override
    public final void dispose() {
        if (onComplete != task.getAndSet(onComplete)) {
            schedule();
        }
    }

    private void iterate() {
        task.get().run();

        var is = state.updateAndGet(it -> it == COMPLETED ? COMPLETED : it - 1);
        if (is == RUNNING) executor.execute(this::iterate);
    }
}
