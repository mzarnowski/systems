package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Pipe<A> extends Component {
    Component forEach(Consumer<A> consumer);
    <B> Pipe<B> map(Function<A, B> f);
    Pipe<A> filter(Predicate<A> predicate);

    <B> Pipe<B> adapt(Function<Flow<A, A>, Flow<A, B>> f);
    Component consume(Function<Flow<A, A>, Flow.Terminal<A>> f);

    Pipe<A> onComplete(Runnable task);
    Pipe<A> onError(ErrorHandler handler);
}
