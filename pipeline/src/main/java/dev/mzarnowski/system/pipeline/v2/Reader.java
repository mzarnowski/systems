package dev.mzarnowski.system.pipeline.v2;

/*internal*/ class Reader<A> implements Claimable, Upstream.Of<A> {
    private final Upstream buffer;

    Reader(Upstream buffer) {
        this.buffer = buffer;
    }

    @Override
    public void request() {
        buffer.request();
    }

    @Override
    public int claim(int amount) {
        return 0;
    }

    @Override
    public A read(int offset) {
        return null;
    }
}
