package dev.mzarnowski.system.pipeline.foo;

import dev.mzarnowski.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

abstract class Pump implements Disposable, Runnable {
    // IDLE <-> RUNNING <-> STACKED
    // not COMPLETED -> COMPLETING -> COMPLETED
    static final int IDLE = 0;
    static final int RUNNING = 1;
    static final int STACKED = 2;
    static final int COMPLETING = 3;
    static final int COMPLETED = 4;

    private final Executor executor;
    private final AtomicInteger state = new AtomicInteger();
    private final Runnable onComplete;

    public Pump(Executor executor, Runnable onComplete) {
        this.executor = executor;
        this.onComplete = onComplete;
    }

    protected abstract void process();      // TODO how to call it?
    protected abstract boolean complete();  // TODO how to call it?

    public void run() {
        if (IDLE == state.getAndUpdate(it -> it < STACKED ? it + 1 : it)) {
            executor.execute(this::pump);
        }
    }

    public void dispose() {
        if (IDLE == state.getAndUpdate(it -> Math.max(COMPLETING, it))) {
            executor.execute(this::drain);
        }
    }

    private void pump() {
        process();

        var was = state.getAndUpdate(it -> it <= STACKED ? it - 1 : it);
        if (was == STACKED) executor.execute(this::pump);
        else if (was == COMPLETING) executor.execute(this::drain);
    }


    private void drain() {
        if (complete()) {
            state.set(COMPLETED);
            executor.execute(onComplete);
        } else {
            executor.execute(this::drain);
        }
    }

}

abstract class Source implements Upstream {
    private final Pump pump;
    private final Runnable onComplete = null;

    protected final Writer downstream = null;

    Source(Executor executor, Pump pump) {
        this.pump = new Pump(executor, onComplete) {
            @Override
            protected void process() {
                var capacity = downstream.claim();
                if (capacity > 0) pull(capacity);
            }

            @Override
            protected boolean complete() {
                return true;
//                var available = downstream.claim();
//                if (available == 0) return false;
//
//                pull(available);
//                return true;
            }
        };
    }

    protected abstract void pull(int amount);

    @Override
    public final void request() {
        pump.run();
    }
}

abstract class Adapter implements Upstream, Downstream {
    private final Pump pump;
    private final Runnable onComplete = null;
    protected final Reader upstream = null;
    protected final Writer downstream = null;

    Adapter(Executor executor) {
        pump = new Pump(executor, onComplete) {
            @Override
            protected void process() {
                int capacity = downstream.claim();
                if (capacity == 0) return;
                int available = upstream.claim();
                if (available == 0) return;

                pull(available);
            }

            @Override
            protected boolean complete() {
                int available = upstream.claim();
                if (available == 0) return false;
                int capacity = downstream.claim();
                if (capacity == 0) return true;

                pull(capacity);
                return true;
            }
        };
    }

    protected abstract void pull(int amount);

    @Override
    public final void request() {
        pump.run();
    }

    @Override
    public final void committed(int ammout) {
        pump.run();
    }
}

abstract class Sink implements Downstream {
    private final Pump pump;
    private final Runnable onComplete = null;
    protected final Reader upstream = null;

    Sink(Executor executor) {
        pump = new Pump(executor, onComplete) {
            @Override
            protected void process() {
                var available = upstream.claim();
                if (available > 0) pull(available);
            }

            @Override
            protected boolean complete() {
                var available = upstream.claim();
                if (available == 0) return false;

                pull(available);
                return true;
            }
        };
    }

    protected abstract void pull(int amount);

    @Override
    public final void committed(int ammout) {
        pump.run();
    }
}

interface Upstream {
    void request();
}

interface Downstream {
    void committed(int amout);
}

interface Writer<A> {
    int claim();
}

interface Reader<A> {
    int claim();
}