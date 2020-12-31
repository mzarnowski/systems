package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

interface Rationed extends Disposable {
    int claim();
    int claim(int atLeast);
    int claim(int atLeast, int atMost);

    void request();
    void release(int amount);
}
