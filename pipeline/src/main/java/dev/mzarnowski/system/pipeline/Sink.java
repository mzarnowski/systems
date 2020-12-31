package dev.mzarnowski.system.pipeline;

import java.util.concurrent.ScheduledExecutorService;

public abstract class Sink extends Task implements Component {
    private final OneTimeJob onComplete = new OneTimeJob(super::dispose);

    public Sink(ScheduledExecutorService scheduler) {
        super(scheduler);
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
