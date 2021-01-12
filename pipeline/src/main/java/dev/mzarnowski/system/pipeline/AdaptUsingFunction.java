package dev.mzarnowski.system.pipeline;

import java.util.function.Function;

final class AdaptUsingFunction<A, B> extends Adapter<A, B> {
    private final Function<A, B> f;

    AdaptUsingFunction(Pipeline owner, Reader<A> upstream, Function<A, B> f) {
        super(owner, upstream);
        this.f = f;
    }

    @Override
    protected void move(int amount) {
        for (int i = 0; i < amount; i++) {
            var next = upstream.read(i);
            this.write(i, f.apply(next));
        }
        upstream.release(amount);
        this.release(amount);
    }
}
