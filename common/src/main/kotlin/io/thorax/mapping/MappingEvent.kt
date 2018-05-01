package io.thorax.mapping

import io.thorax.Event

/**
 * Applies the result mapping to the upstream event when evaluated.
 */
class MappingEvent<X : Any, out T : Any, Y : Any, S : Any>(
    private val upstream: Event<Y, S>,
    private val resultMapping: ResultMapping<X, T, S>,
    private val failureMapping: FailureMapping<X, T, Y>
) : Event<X, T> {

    interface ResultMapping<X : Any, out T : Any, in S : Any> {
        fun map(result: S): Event<X, T>

        class Identity<X : Any, T : Any> : ResultMapping<X, T, T> {
            override fun map(result: T): Event<X, T> = Event.Result(result)
        }

        class Block<X : Any, out T : Any, in S : Any>(
            private val block: (S) -> Event<X, T>
        ) : MappingEvent.ResultMapping<X, T, S> {
            override fun map(result: S): Event<X, T> = block(result)
        }
    }

    interface FailureMapping<X : Any, out T : Any, in Y : Any> {
        fun map(reason: Y): Event<X, T>

        class Identity<X : Any, out T : Any> : FailureMapping<X, T, X> {
            override fun map(reason: X): Event<X, T> = Event.Failure(reason)
        }

        class Block<X : Any, out T : Any, in Y : Any>(
            private val block: (Y) -> Event<X, T>
        ) : MappingEvent.FailureMapping<X, T, Y> {
            override fun map(reason: Y): Event<X, T> = block(reason)
        }
    }

    override fun evaluate(delegate: Event.Delegate<X, T>) = upstream.evaluate(ResultDelegate(
            failureMapping = failureMapping,
            resultMapping = resultMapping,
            downstream = delegate
    ))

    private class ResultDelegate<X : Any, T : Any, Y : Any, in S : Any>(
        private val resultMapping: ResultMapping<X, T, S>,
        private val failureMapping: FailureMapping<X, T, Y>,
        private val downstream: Event.Delegate<X, T>
    ) : Event.Delegate<Y, S> {
        override fun eventResult(event: Event<Y, S>, result: S) {
            resultMapping.map(result).evaluate(downstream)
        }

        override fun eventFailure(event: Event<Y, S>, reason: Y) {
            failureMapping.map(reason).evaluate(downstream)
        }
    }
}
