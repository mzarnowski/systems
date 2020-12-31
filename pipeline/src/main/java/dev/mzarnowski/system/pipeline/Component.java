package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

interface Component extends Disposable {
    Component onComplete(Runnable runnable);
}