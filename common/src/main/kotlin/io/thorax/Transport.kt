package io.thorax

interface Transport<X : Any, out T : Any, in A : Any> {
    interface Endpoint<X : Any, out T : Any> {
        fun connect(): Source<X, T>
    }

    fun endpoint(address: A): Endpoint<X, T>
}
