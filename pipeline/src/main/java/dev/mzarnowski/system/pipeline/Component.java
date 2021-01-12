package dev.mzarnowski.system.pipeline;

abstract class Component {
    protected final Pipeline owner;
    protected final OneTimeJob onComplete = new OneTimeJob();

    protected Component(Pipeline owner) {
        this.owner = owner;
    }

    public abstract Component onComplete(Runnable task);

    protected final boolean isDisposed() {
        return onComplete.wasStarted();
    }
}