package io.thorax

interface Schedule : Subscription {
    fun post(block: () -> Subscription)
}
