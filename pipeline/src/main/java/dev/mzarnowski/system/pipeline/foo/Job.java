package dev.mzarnowski.system.pipeline.foo;

// IDLE, RUNNING, STACKED, COMPLETING, COMPLETED



abstract class Job<Upstream, Downstream> {
    protected final Upstream upstream;
    protected final Downstream downstream;

    protected Job(Upstream upstream, Downstream downstream) {
        this.upstream = upstream;
        this.downstream = downstream;
    }

    /**
     * @return true if the drain was successful,
     * false if it should be done again
     */
    abstract boolean tryDrain();

    abstract void pull(int amount);
}

abstract class Pull<A, Upstream> extends Job<Upstream, Writer<A>> {
    protected Pull(Upstream upstream, Writer<A> downstream) {
        super(upstream, downstream);
    }

    @Override
    final boolean tryDrain() {
        return true;
    }
}