package io.thorax.schedule

import io.thorax.Subscription
import kotlin.test.Test
import kotlin.test.assertEquals

class SingleScheduleTest {
    @Test fun `post() sequential`() {
        var initialSubscribeCount = 0
        var initialUnsubscribeCount = 0

        val initial: () -> Subscription = {
            initialSubscribeCount++
            object : Subscription {
                override fun unsubscribe(delegate: Subscription.Delegate) {
                    initialUnsubscribeCount++
                    delegate.subscriptionEnd(this)
                }
            }
        }

        var subsequentSubscribeCount = 0
        var subsequentUnsubscribeCount = 0
        val subsequent: () -> Subscription = {
            subsequentSubscribeCount++
            object : Subscription {
                override fun unsubscribe(delegate: Subscription.Delegate) {
                    subsequentUnsubscribeCount++
                    delegate.subscriptionEnd(this)
                }
            }
        }

        val schedule = Single()
        schedule.post(initial)

        assertEquals(1, initialSubscribeCount)
        assertEquals(0, initialUnsubscribeCount)
        assertEquals(0, subsequentSubscribeCount)
        assertEquals(0, subsequentUnsubscribeCount)

        schedule.post(subsequent)

        assertEquals(1, initialSubscribeCount)
        assertEquals(1, initialUnsubscribeCount)
        assertEquals(1, subsequentSubscribeCount)
        assertEquals(0, subsequentUnsubscribeCount)

        var scheduleUnsubscribeCount = 0
        schedule.unsubscribe(object : Subscription.Delegate {
            override fun subscriptionEnd(subscription: Subscription) {
                scheduleUnsubscribeCount++
            }
        })
        assertEquals(1, scheduleUnsubscribeCount)

        assertEquals(1, initialSubscribeCount)
        assertEquals(1, initialUnsubscribeCount)
        assertEquals(1, subsequentSubscribeCount)
        assertEquals(1, subsequentUnsubscribeCount)
    }

    @Test fun `post() recursive`() {
        val schedule = Single()

        var subsequentSubscribeCount = 0
        var subsequentUnsubscribeCount = 0
        val subsequent: () -> Subscription = {
            subsequentSubscribeCount++
            object : Subscription {
                override fun unsubscribe(delegate: Subscription.Delegate) {
                    subsequentUnsubscribeCount++
                    delegate.subscriptionEnd(this)
                }
            }
        }

        var initialSubscribeCount = 0
        var initialUnsubscribeCount = 0
        val initial: () -> Subscription = {
            initialSubscribeCount++
            schedule.post(subsequent)
            object : Subscription {
                override fun unsubscribe(delegate: Subscription.Delegate) {
                    initialUnsubscribeCount++
                    delegate.subscriptionEnd(this)
                }
            }
        }

        schedule.post(initial)

        assertEquals(1, initialSubscribeCount)
        assertEquals(1, initialUnsubscribeCount)
        assertEquals(1, subsequentSubscribeCount)
        assertEquals(0, subsequentUnsubscribeCount)

        var scheduleUnsubscribeCount = 0
        schedule.unsubscribe(object : Subscription.Delegate {
            override fun subscriptionEnd(subscription: Subscription) {
                scheduleUnsubscribeCount++
            }
        })
        assertEquals(1, scheduleUnsubscribeCount)

        assertEquals(1, initialSubscribeCount)
        assertEquals(1, initialUnsubscribeCount)
        assertEquals(1, subsequentSubscribeCount)
        assertEquals(1, subsequentUnsubscribeCount)
    }
}
