package io.thorax

interface Subscription {
    interface Delegate {
        fun subscriptionEnd(subscription: Subscription)
    }

    fun unsubscribe(delegate: Delegate)
}
