package dev.mzarnowski.system.pipeline;

import java.util.ArrayList;
import java.util.List;

// TODO use name that suggests it runs only once?
final class Callback implements Runnable {
    private final List<Runnable> tasks = new ArrayList<>();
    private volatile boolean isDisposed;

    Callback(Runnable... tasks) {
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
