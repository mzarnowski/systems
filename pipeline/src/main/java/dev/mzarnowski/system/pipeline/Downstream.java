package dev.mzarnowski.system.pipeline;

public interface Downstream {
    void onAvailable();
    void onComplete();

    interface Of<A> extends Downstream {
        void write(int offset, A value);
        void release(int amount);
    }
}
