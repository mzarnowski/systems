package dev.mzarnowski.system.pipeline;

import java.util.concurrent.ScheduledExecutorService;

public abstract class Sink extends Task implements Component {
    private final Callback onComplete = new Callback();

    public Sink(ScheduledExecutorService scheduler) {
        super(scheduler);
        onComplete.add(super::dispose);
    }

    @Override
    public Component onComplete(Runnable runnable) {
        onComplete.add(runnable);
        return this;
    }

    @Override
    public final void dispose() {
        onComplete.run();
    }
}
