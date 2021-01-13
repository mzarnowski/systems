package dev.mzarnowski.system.pipeline;

import dev.mzarnowski.Disposable;

import java.util.function.BiConsumer;

@FunctionalInterface
interface ErrorHandler extends BiConsumer<Disposable, Throwable> {
}
