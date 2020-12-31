package dev.mzarnowski.system.pipeline;

import java.util.concurrent.atomic.AtomicReference;

final class RunOnce implements Runnable {
    private final AtomicReference<Runnable> task;

    RunOnce(Runnable runnable) {
        this.task = new AtomicReference<>(runnable);
    }

    @Override
    public void run() {
        var task = this.task.getAndSet(null);
        if (task != null) task.run();
    }
}
