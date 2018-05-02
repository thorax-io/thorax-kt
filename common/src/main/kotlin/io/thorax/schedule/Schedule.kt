package io.thorax.schedule

import io.thorax.Schedule
import io.thorax.Source
import io.thorax.Transport
import io.thorax.mapping.MappingEvent
import io.thorax.mapping.MappingSource
import io.thorax.mapping.MappingTransport

fun <X : Any, T : Any> Source<X, Source<X, T>>.spawn(
    schedule: Schedule
): Source<X, T> = ScheduleSource(
        upstream = MappingSource(
                upstream = this,
                resultMapping = ScheduleEvent.Mapping(
                        schedule = schedule
                ),
                failureMapping = MappingEvent.FailureMapping.Identity()
        ),
        schedule = schedule
)

fun <X : Any, T : Any, A : Any> Transport<X, Source<X, T>, A>.spawn(
    schedule: Schedule
): Transport<X, T, A> = ScheduleTransport(
        upstream = MappingTransport(
                upstream = this,
                resultMapping = ScheduleEvent.Mapping(
                        schedule = schedule
                ),
                failureMapping = MappingEvent.FailureMapping.Identity()
        ),
        schedule = schedule
)
