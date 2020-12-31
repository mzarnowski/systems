package dev.mzarnowski.system.pipeline;

interface Rationed {
    int claim();
    int claim(int atLeast);
    int claim(int atLeast, int atMost);

    void release(int amount);
}
