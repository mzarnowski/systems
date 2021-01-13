package dev.mzarnowski.system.pipeline;

import java.util.HashSet;
import java.util.Set;

abstract class FeedbackComponent implements Component {
    protected final Pipeline owner;
    protected final OneTimeJob onComplete = new OneTimeJob();
    protected final Set<ErrorHandler> onError = new HashSet<>();

    protected FeedbackComponent(Pipeline owner) {
        this.owner = owner;
    }

    protected final boolean isDisposed() {
        return onComplete.wasStarted();
    }
}