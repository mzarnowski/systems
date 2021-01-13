package dev.mzarnowski.system.pipeline;

final class FlowToBuffer<A, B> extends Flow.Terminal<A> {
    private final Flow<A, B> source;
    private final Buffer<B> buffer;

    FlowToBuffer(Flow<A, B> source, Buffer<B> buffer) {
        this.source = source;
        this.buffer = buffer;
    }

    @Override
    protected FlowStaged<A> build() {
        var stage = new FlowStaged<B>() {
            private int offset = 0;

            @Override
            public void accept(B next) {
                buffer.write(offset++, next);
            }

            @Override
            public void commit() {
                buffer.release(offset);
                offset = 0;
            }
        };

        return source.build(stage);
    }
}
