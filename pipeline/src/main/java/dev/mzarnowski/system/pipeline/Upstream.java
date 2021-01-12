package dev.mzarnowski.system.pipeline;

public interface Upstream {
    void request();

    interface Of<A> extends Upstream {
        A read(int offset);
        void release(int amount);
    }
}
