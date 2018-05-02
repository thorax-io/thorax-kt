package io.thorax.schedule

import io.thorax.Schedule
import io.thorax.Source
import io.thorax.Transport

/**
 * Returns a source registered with the given schedule when connected.
 */
class ScheduleTransport<X : Any, out T : Any, in A : Any>(
    private val upstream: Transport<X, T, A>,
    private val schedule: Schedule
) : Transport<X, T, A> {

    override fun connect(address: A): Source<X, T> = ScheduleSource(
            upstream = upstream.connect(address),
            schedule = schedule
    )
}
