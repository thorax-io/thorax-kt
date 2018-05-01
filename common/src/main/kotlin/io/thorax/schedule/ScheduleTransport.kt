package io.thorax.schedule

import io.thorax.Schedule
import io.thorax.Source
import io.thorax.Transport

/**
 * Provides an endpoint which returns a source registered with the given schedule when connected.
 */
class ScheduleTransport<X : Any, out T : Any, in A : Any>(
    private val upstream: Transport<X, T, A>,
    private val schedule: Schedule
) : Transport<X, T, A> {

    override fun endpoint(address: A): Transport.Endpoint<X, T> = Endpoint(
            endpoint = upstream.endpoint(address),
            schedule = schedule
    )

    private class Endpoint<X : Any, out T : Any>(
        private val endpoint: Transport.Endpoint<X, T>,
        private val schedule: Schedule
    ) : Transport.Endpoint<X, T> {
        override fun connect(): Source<X, T> = ScheduleSource(
                upstream = endpoint.connect(),
                schedule = schedule
        )
    }
}
