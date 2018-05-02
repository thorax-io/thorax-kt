package io.thorax

interface Transport<X : Any, out T : Any, in A : Any> {
    fun connect(address: A): Source<X, T>
}
