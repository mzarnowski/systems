package dev.mzarnowski.system.pipeline.v2;

public interface Upstream {
    void request();

    interface Of<A> extends Upstream {
        A read(int offset);
    }
}
