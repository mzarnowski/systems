package dev.mzarnowski.system.pipeline;

final class SinkToFlow<A> extends Sink<A> {
    private final FlowStaged<A> flow;

    SinkToFlow(Pipeline owner, Reader<A> upstream, Flow.Terminal<A> terminal) {
        super(owner, upstream);
        this.flow = terminal.build();
        schedule();
    }

    @Override
    protected void move(int amount) {
        upstream.request();
        if (amount == 0) return;

        for (int i = 0; i < amount; i++) {
            var next = upstream.read(i);
            flow.accept(next);
        }
        upstream.release(amount);
        flow.commit();
    }
}
