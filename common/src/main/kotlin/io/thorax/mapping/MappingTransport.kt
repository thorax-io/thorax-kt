package io.thorax.mapping

import io.thorax.Source
import io.thorax.Transport

class MappingTransport<X : Any, out T : Any, Y : Any, S : Any, in A : Any>(
    private val upstream: Transport<Y, S, A>,
    private val resultMapping: MappingEvent.ResultMapping<X, T, S>,
    private val failureMapping: MappingEvent.FailureMapping<X, T, Y>
) : Transport<X, T, A> {
    override fun connect(address: A): Source<X, T> = MappingSource(
            upstream = upstream.connect(address),
            failureMapping = failureMapping,
            resultMapping = resultMapping
    )
}
