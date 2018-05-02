package io.thorax.schedule

import io.thorax.Schedule
import io.thorax.Subscription

/**
 * Single subscription of added sources.
 *
 * Ensures that previous sources are unsubscribed before a new subscription is added.
 */
actual class Single : Schedule {
    private var current: Subscription = InitialSubscription()
    private val last: Subscription = FinalSubscription()
    private val queue = mutableListOf<() -> Subscription>()

    override fun post(block: () -> Subscription) {
        if (queue.enqueue(block) == 0) do {
            val size = iterate()
        } while (size > 0)
    }

    override fun unsubscribe(delegate: Subscription.Delegate) = post {
        last.apply {
            unsubscribe(delegate)
        }
    }

    @Synchronized
    private fun iterate(): Int = if (current !== last) {
        queue.shift { block ->
            current.unsubscribe(object : Subscription.Delegate {
                override fun subscriptionEnd(subscription: Subscription) {
                    val next = block()
                    synchronized(this@Single) {
                        current = next
                    }
                }
            })
        }
    } else {
        queue.flush()
    }

    /**
     * Adds an item to the queue, returns the original size.
     */
    @Synchronized
    private fun <E> MutableList<E>.enqueue(element: E): Int = size.also {
        add(element)
    }

    /**
     * Evaluates the given block with the item removed from the queue, returns the new size.
     */
    @Synchronized
    private fun <E> MutableList<E>.shift(block: (element: E) -> Unit): Int = first()
            .apply(block)
            .also { remove(it) }
            .let { size }

    /**
     * Clears all elements from the queue and returns the new size.
     */
    @Synchronized
    private fun <E> MutableList<E>.flush(): Int = clear().let { 0 }

    private class InitialSubscription : Subscription {
        override fun unsubscribe(delegate: Subscription.Delegate) {
            delegate.subscriptionEnd(this)
        }
    }

    private class FinalSubscription : Subscription {
        override fun unsubscribe(delegate: Subscription.Delegate) {
            delegate.subscriptionEnd(this)
        }
    }
}
