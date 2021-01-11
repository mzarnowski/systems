package dev.mzarnowski.system.pipeline.v2;

public interface Downstream {
    void onAvailable();

    interface Of<A> extends Downstream {
        void write(int offset, A value);
    }
}
