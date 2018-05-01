package io.thorax.mapping

import io.thorax.Source
import io.thorax.Transport

class MappingTransport<X : Any, out T : Any, Y : Any, S : Any, in A : Any>(
    private val upstream: Transport<Y, S, A>,
    private val resultMapping: MappingEvent.ResultMapping<X, T, S>,
    private val failureMapping: MappingEvent.FailureMapping<X, T, Y>
) : Transport<X, T, A> {
    override fun endpoint(address: A): Transport.Endpoint<X, T> = Endpoint(
            endpoint = upstream.endpoint(address),
            failureMapping = failureMapping,
            resultMapping = resultMapping
    )

    private class Endpoint<X : Any, out T : Any, Y : Any, S : Any>(
        private val endpoint: Transport.Endpoint<Y, S>,
        private val resultMapping: MappingEvent.ResultMapping<X, T, S>,
        private val failureMapping: MappingEvent.FailureMapping<X, T, Y>
    ) : Transport.Endpoint<X, T> {
        override fun connect(): Source<X, T> = MappingSource(
                upstream = endpoint.connect(),
                failureMapping = failureMapping,
                resultMapping = resultMapping
        )
    }
}
