package io.thorax

interface Source<X : Any, out T : Any> {
    interface Delegate<X : Any, in T : Any> {
        fun sourceEvent(source: Source<X, T>, event: Event<X, T>)
    }

    fun subscribe(delegate: Delegate<X, T>): Subscription
}
