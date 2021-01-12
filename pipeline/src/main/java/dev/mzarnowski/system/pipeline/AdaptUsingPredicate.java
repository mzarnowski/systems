package dev.mzarnowski.system.pipeline;

import java.util.function.Predicate;

final class AdaptUsingPredicate<A> extends Adapter<A, A> {
    private final Predicate<A> p;

    AdaptUsingPredicate(Pipeline owner, Reader<A> upstream, Predicate<A> p) {
        super(owner, upstream);
        this.p = p;
    }

    @Override
    protected void move(int amount) {
        var moved = 0;
        for (int i = 0; i < amount; i++) {
            var next = upstream.read(i);
            if (p.test(next)) this.write(moved++, next);
        }
        upstream.release(amount);
        this.release(moved);
    }
}
