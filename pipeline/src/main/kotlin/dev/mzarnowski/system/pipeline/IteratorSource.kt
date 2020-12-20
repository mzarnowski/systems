package dev.mzarnowski.system.pipeline

class IterableSource<A>(private val iterable: Iterable<A>) : (Writer<A>) -> () -> Task.Result {
    override fun invoke(writer: Writer<A>): () -> Task.Result {
        val iterator = iterable.iterator()

        return {
            val available = writer.claim(1, 1)
            if (available == 0) Task.Continue
            else {
                var n = 0
                while (iterator.hasNext() && n < available) {
                    val next = iterator.next()
                    writer.write(n++, next)
                }

                writer.release(n)
                if (iterator.hasNext()) Task.Continue else Task.Break
            }
        }
    }
}