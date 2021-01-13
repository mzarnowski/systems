package dev.mzarnowski.system.pipeline;

abstract class FlowDownstream<A, B, C> extends Flow<A, C> {
    private final Flow<A, B> source;

    protected FlowDownstream(Flow<A, B> source) {
        this.source = source;
    }

    @Override
    FlowStaged<A> build(FlowStaged<C> next) {
        return source.build(staged(next));
    }

    protected abstract FlowStaged<B> staged(FlowStaged<C> next);
}
