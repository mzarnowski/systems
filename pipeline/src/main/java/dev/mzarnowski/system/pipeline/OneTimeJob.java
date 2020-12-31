package dev.mzarnowski.system.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class OneTimeJob implements Runnable {
    private final List<Runnable> tasks = new ArrayList<>();
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    OneTimeJob(Runnable... tasks) {
        for (Runnable task : tasks) add(task);
    }

    void add(Runnable task) {
        task = new RunOnce(task);

        tasks.add(task);
        if (isStarted.get()) task.run();
    }

    @Override
    public void run() {
        if (isStarted.compareAndSet(false, true)) {
            tasks.forEach(Runnable::run);
        }
    }

    public boolean wasStarted() {
        return isStarted.get();
    }
}
