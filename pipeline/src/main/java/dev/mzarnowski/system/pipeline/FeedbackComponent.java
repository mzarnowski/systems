package dev.mzarnowski.system.pipeline;

abstract class FeedbackComponent implements Component {
    protected final Pipeline owner;
    protected final OneTimeJob onComplete = new OneTimeJob();

    protected FeedbackComponent(Pipeline owner) {
        this.owner = owner;
    }

    protected final boolean isDisposed() {
        return onComplete.wasStarted();
    }
}