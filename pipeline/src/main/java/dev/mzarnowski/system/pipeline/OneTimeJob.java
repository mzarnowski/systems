package dev.mzarnowski.system.pipeline;

import java.util.ArrayList;
import java.util.List;

final class OneTimeJob implements Runnable {
    private final List<Runnable> tasks = new ArrayList<>();
    private volatile boolean isDisposed;

    OneTimeJob(Runnable... tasks) {
        for (Runnable task : tasks) add(task);
    }

    void add(Runnable task) {
        task = new RunOnce(task);

        tasks.add(task);
        if (isDisposed) task.run();
    }

    @Override
    public void run() {
        isDisposed = true;
        tasks.forEach(Runnable::run);
    }
}
