package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

public interface Writer<A> extends Disposable, Rationed {
    void set(int offset, A value);
}
