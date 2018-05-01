package io.thorax

interface Event<X : Any, out T : Any> {
    interface Delegate<X : Any, in T : Any> {
        fun eventResult(event: Event<X, T>, result: T)
        fun eventFailure(event: Event<X, T>, reason: X)
    }

    fun evaluate(delegate: Delegate<X, T>)

    class Result<X : Any, out T : Any>(private val value: T) : Event<X, T> {
        override fun evaluate(delegate: Delegate<X, T>) = delegate.eventResult(this, value)
    }

    class Failure<X : Any, out T : Any>(private val reason: X) : Event<X, T> {
        override fun evaluate(delegate: Delegate<X, T>) = delegate.eventFailure(this, reason)
    }
}
