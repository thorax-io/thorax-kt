package io.thorax.mapping

import io.thorax.Event
import io.thorax.Source
import io.thorax.Subscription

class MappingSource<X : Any, out T : Any, Y : Any, S : Any>(
    private val upstream: Source<Y, S>,
    private val resultMapping: MappingEvent.ResultMapping<X, T, S>,
    private val failureMapping: MappingEvent.FailureMapping<X, T, Y>
) : Source<X, T> {

    override fun subscribe(
        delegate: Source.Delegate<X, T>
    ): Subscription = upstream.subscribe(SourceDelegate(
            downstream = delegate,
            parent = this,
            failureMapping = failureMapping,
            resultMapping = resultMapping
    ))

    private class SourceDelegate<X : Any, T : Any, Y : Any, in S : Any>(
        private val downstream: Source.Delegate<X, T>,
        private val parent: Source<X, T>,
        private val resultMapping: MappingEvent.ResultMapping<X, T, S>,
        private val failureMapping: MappingEvent.FailureMapping<X, T, Y>
    ) : Source.Delegate<Y, S> {

        override fun sourceEvent(source: Source<Y, S>, event: Event<Y, S>) {
            parent.emit(MappingEvent(
                    upstream = event,
                    failureMapping = failureMapping,
                    resultMapping = resultMapping
            ))
        }

        private fun Source<X, T>.emit(event: Event<X, T>) {
            downstream.sourceEvent(this, event)
        }
    }
}
