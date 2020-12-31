package dev.mzarnowski.system.pipeline;

import java.util.function.Consumer;

public interface Pipe<A> extends Component {
    Component forEach(Consumer<A> f);
}
