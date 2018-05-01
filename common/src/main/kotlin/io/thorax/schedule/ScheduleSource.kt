package io.thorax.schedule

import io.thorax.Schedule
import io.thorax.Source
import io.thorax.Subscription

/**
 * Adds a source to the given schedule when subscribed.
 */
class ScheduleSource<X : Any, out T : Any>(
    private val upstream: Source<X, T>,
    private val schedule: Schedule
) : Source<X, T> {

    override fun subscribe(delegate: Source.Delegate<X, T>): Subscription = schedule.post {
        upstream.subscribe(delegate)
    }.let { schedule }
}
