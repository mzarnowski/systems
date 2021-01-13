package dev.mzarnowski.system.pipeline;

final class AdaptUsingFlow<A, B> extends Adapter<A, B> {
    private final FlowStaged<A> stream;

    AdaptUsingFlow(Pipeline owner, Reader<A> upstream, Flow<A, B> flow) {
        super(owner, upstream);
        this.stream = flow.forEach(this).build();
    }

    @Override
    protected void move(int amount) {
        var offset = 0;
        while (offset < amount) {
            var next = upstream.read(offset++);
            stream.accept(next);
        }
        upstream.release(offset);
        stream.commit();
    }
}
