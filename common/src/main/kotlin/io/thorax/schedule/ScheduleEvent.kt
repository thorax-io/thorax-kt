package io.thorax.schedule

import io.thorax.Event
import io.thorax.Schedule
import io.thorax.Source
import io.thorax.mapping.MappingEvent

/**
 * Creates a new subscription from the upstream source and schedules it when evaluated.
 */
class ScheduleEvent<X : Any, out T : Any>(
    private val upstream: Source<X, T>,
    private val schedule: Schedule
) : Event<X, T> {

    /**
     * Creates a new subscription from the upstream event and stores it in the given slot when
     * evaluated.
     */
    class Mapping<X : Any, T : Any>(
        private val schedule: Schedule
    ) : MappingEvent.ResultMapping<X, T, Source<X, T>> {

        override fun map(result: Source<X, T>): Event<X, T> = ScheduleEvent(
                upstream = result,
                schedule = schedule
        )
    }

    override fun evaluate(delegate: Event.Delegate<X, T>) = schedule.post {
        upstream.subscribe(SourceDelegate(delegate))
    }

    private class SourceDelegate<X : Any, in T : Any>(
        private val downstream: Event.Delegate<X, T>
    ) : Source.Delegate<X, T> {
        override fun sourceEvent(source: Source<X, T>, event: Event<X, T>) {
            event.evaluate(downstream)
        }
    }
}
