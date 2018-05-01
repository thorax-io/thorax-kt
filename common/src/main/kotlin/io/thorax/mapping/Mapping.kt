package io.thorax.mapping

import io.thorax.Event
import io.thorax.Source
import io.thorax.Transport

//region mapEvent
fun <X : Any, T : Any, Y : Any, S : Any> Event<Y, S>.mapEvent(
    resultMapping: MappingEvent.ResultMapping<X, T, S>,
    failureMapping: MappingEvent.FailureMapping<X, T, Y>
): Event<X, T> = MappingEvent(
        upstream = this,
        resultMapping = resultMapping,
        failureMapping = failureMapping
)

fun <X : Any, T : Any, Y : Any, S : Any> Source<Y, S>.mapEvent(
    resultMapping: MappingEvent.ResultMapping<X, T, S>,
    failureMapping: MappingEvent.FailureMapping<X, T, Y>
): Source<X, T> = MappingSource(
        upstream = this,
        resultMapping = resultMapping,
        failureMapping = failureMapping
)

fun <X : Any, T : Any, Y : Any, S : Any, A : Any> Transport<Y, S, A>.mapEvent(
    resultMapping: MappingEvent.ResultMapping<X, T, S>,
    failureMapping: MappingEvent.FailureMapping<X, T, Y>
): Transport<X, T, A> = MappingTransport(
        upstream = this,
        resultMapping = resultMapping,
        failureMapping = failureMapping
)
//endregion

//region mapResult
fun <X : Any, T : Any, S : Any> Event<X, S>.mapResult(
    mapping: MappingEvent.ResultMapping<X, T, S>
): Event<X, T> = mapEvent(
        resultMapping = mapping,
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, S : Any> Event<X, S>.mapResult(
    block: (S) -> Event<X, T>
): Event<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Block(block),
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, S : Any> Source<X, S>.mapResult(
    mapping: MappingEvent.ResultMapping<X, T, S>
): Source<X, T> = mapEvent(
        resultMapping = mapping,
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, S : Any> Source<X, S>.mapResult(
    block: (S) -> Event<X, T>
): Source<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Block(block),
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, S : Any, A : Any> Transport<X, S, A>.mapResult(
    mapping: MappingEvent.ResultMapping<X, T, S>
): Transport<X, T, A> = mapEvent(
        resultMapping = mapping,
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, S : Any, A : Any> Transport<X, S, A>.mapResult(
    block: (S) -> Event<X, T>
): Transport<X, T, A> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Block(block),
        failureMapping = MappingEvent.FailureMapping.Identity()
)

fun <X : Any, T : Any, Y : Any, S : Any> MappingEvent.FailureMapping<X, S, Y>.mapResult(
    mapping: MappingEvent.ResultMapping<X, T, S>
): MappingEvent.FailureMapping<X, T, Y> {
    return object : MappingEvent.FailureMapping<X, T, Y> {
        override fun map(reason: Y): Event<X, T> = MappingEvent(
                upstream = this@mapResult.map(reason),
                resultMapping = mapping,
                failureMapping = MappingEvent.FailureMapping.Identity()
        )
    }
}

fun <X : Any, T : Any, Y : Any, S : Any> MappingEvent.FailureMapping<X, S, Y>.mapResult(
    block: (S) -> Event<X, T>
): MappingEvent.FailureMapping<X, T, Y> = mapResult(
        mapping = MappingEvent.ResultMapping.Block(block)
)
//endregion

//region mapFailure
fun <X : Any, T : Any, Y : Any> Event<Y, T>.mapFailure(
    mapping: MappingEvent.FailureMapping<X, T, Y>
): Event<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = mapping
)

fun <X : Any, T : Any, Y : Any> Event<Y, T>.mapFailure(
    block: (Y) -> Event<X, T>
): Event<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = MappingEvent.FailureMapping.Block(block)
)

fun <X : Any, T : Any, Y : Any> Source<Y, T>.mapFailure(
    mapping: MappingEvent.FailureMapping<X, T, Y>
): Source<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = mapping
)

fun <X : Any, T : Any, Y : Any> Source<Y, T>.mapFailure(
    block: (Y) -> Event<X, T>
): Source<X, T> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = MappingEvent.FailureMapping.Block(block)
)

fun <X : Any, T : Any, Y : Any, A : Any> Transport<Y, T, A>.mapFailure(
    mapping: MappingEvent.FailureMapping<X, T, Y>
): Transport<X, T, A> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = mapping
)

fun <X : Any, T : Any, Y : Any, A : Any> Transport<Y, T, A>.mapFailure(
    block: (Y) -> Event<X, T>
): Transport<X, T, A> = mapEvent(
        resultMapping = MappingEvent.ResultMapping.Identity(),
        failureMapping = MappingEvent.FailureMapping.Block(block)
)

fun <X : Any, T : Any, Y : Any, S : Any> MappingEvent.ResultMapping<Y, T, S>.mapFailure(
    mapping: MappingEvent.FailureMapping<X, T, Y>
): MappingEvent.ResultMapping<X, T, S> {
    return object : MappingEvent.ResultMapping<X, T, S> {
        override fun map(result: S): Event<X, T> = MappingEvent(
                upstream = this@mapFailure.map(result),
                resultMapping = MappingEvent.ResultMapping.Identity(),
                failureMapping = mapping
        )
    }
}

fun <X : Any, T : Any, Y : Any, S : Any> MappingEvent.ResultMapping<Y, T, S>.mapFailure(
    block: (Y) -> Event<X, T>
): MappingEvent.ResultMapping<X, T, S> = mapFailure(
        mapping = MappingEvent.FailureMapping.Block(block)
)
//endregion

//region mapSource
fun <X : Any, T : Any, S : Any> Source<X, S>.mapSource(
    mapping: MappingEvent.ResultMapping<X, Source<X, T>, S>
): Source<X, Source<X, T>> = mapResult(
        mapping = mapping
)

fun <X : Any, T : Any, S : Any> Source<X, S>.mapSource(
    block: (S) -> Source<X, T>
): Source<X, Source<X, T>> = mapSource(
        mapping = SourceMapping(block)
)

fun <X : Any, T : Any, S : Any, A : Any> Transport<X, S, A>.mapSource(
    mapping: MappingEvent.ResultMapping<X, Source<X, T>, S>
): Transport<X, Source<X, T>, A> = mapResult(
        mapping = mapping
)

fun <X : Any, T : Any, S : Any, A : Any> Transport<X, S, A>.mapSource(
    block: (S) -> Source<X, T>
): Transport<X, Source<X, T>, A> = mapResult(
        mapping = SourceMapping(block)
)

private class SourceMapping<X : Any, out T : Any, in S : Any>(
    private val block: (S) -> Source<X, T>
) : MappingEvent.ResultMapping<X, Source<X, T>, S> {
    override fun map(result: S): Event<X, Source<X, T>> = Event.Result(block(result))
}
//endregion
