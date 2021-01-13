package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;

interface FlowStaged<A> extends Consumer<A> {
    void commit();
}
