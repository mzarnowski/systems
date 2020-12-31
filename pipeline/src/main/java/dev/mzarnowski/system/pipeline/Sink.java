package dev.mzarnowski.system.pipeline;

import java.util.concurrent.ScheduledExecutorService;

public abstract class Sink extends Task implements Component {
    private final Callback onComplete = new Callback();

    public Sink(ScheduledExecutorService scheduler) {
        super(scheduler);
    }

    @Override
    public Component onComplete(Runnable runnable) {
        onComplete.add(runnable);
        return this;
    }

    @Override
    public void dispose() {
        super.dispose();
        onComplete.run();
    }
}
