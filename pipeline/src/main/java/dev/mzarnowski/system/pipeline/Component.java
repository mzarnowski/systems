package dev.mzarnowski.system.pipeline;

public interface Component {
    Component onComplete(Runnable task);
    Component onError(ErrorHandler handler);
}
