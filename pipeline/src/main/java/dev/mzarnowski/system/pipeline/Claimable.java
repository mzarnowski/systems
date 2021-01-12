package dev.mzarnowski.system.pipeline;

/*internal*/ interface Claimable {
    int claim(int atLeast, int atMost);
}
