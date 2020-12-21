package dev.mzarnowski.system.pipeline;

public interface Writer<A> extends Rationed {
    void set(int offset, A value);
}
