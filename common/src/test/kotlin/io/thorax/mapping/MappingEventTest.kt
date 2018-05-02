package io.thorax.mapping

import io.thorax.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class MappingEventTest {
    private data class UpstreamFailure(val reason: String)

    private data class UpstreamResult(val name: String)

    private data class DownstreamFailure(val reason: String)

    private data class DownstreamResult(val message: String)

    private class TestResultEvent<X : Any, T : Any>(
        private val result: T
    ) : Event<X, T> {
        override fun evaluate(delegate: Event.Delegate<X, T>) {
            delegate.eventResult(this, result)
        }

        internal class Delegate<X : Any, T : Any>(
            private val expectedResult: T
        ) : Event.Delegate<X, T> {
            private lateinit var recordedResult: T
            private var count = 0

            override fun eventResult(event: Event<X, T>, result: T) {
                count++
                recordedResult = result
            }

            override fun eventFailure(event: Event<X, T>, reason: X) {
                fail("Failure unexpected: $reason")
            }

            fun verify() {
                assertEquals(1, count)
                assertEquals(expectedResult, recordedResult)
            }
        }
    }

    private class TestFailureEvent<X : Any, T : Any>(
        private val reason: X
    ) : Event<X, T> {
        override fun evaluate(delegate: Event.Delegate<X, T>) {
            delegate.eventFailure(this, reason)
        }

        internal class Delegate<X : Any, T : Any>(
            private val expectedReason: X
        ) : Event.Delegate<X, T> {
            private lateinit var recordedReason: X
            private var count = 0

            override fun eventResult(event: Event<X, T>, result: T) {
                fail("Result unexpected: $result")
            }

            override fun eventFailure(event: Event<X, T>, reason: X) {
                count++
                recordedReason = reason
            }

            fun verify() {
                assertEquals(1, count)
                assertEquals(expectedReason, recordedReason)
            }
        }
    }

    private class ResultMapping<X : Any, S : Any>(
        private val prefix: String,
        private val suffix: String
    ) : MappingEvent.ResultMapping<X, DownstreamResult, S> {
        override fun map(
            result: S
        ): Event<X, DownstreamResult> = TestResultEvent(DownstreamResult(
            message = "$prefix$result$suffix"
        ))
    }

    private class FailureMapping<T : Any, Y : Any>(
        private val prefix: String,
        private val suffix: String
    ) : MappingEvent.FailureMapping<DownstreamFailure, T, Y> {
        override fun map(
            reason: Y
        ): Event<DownstreamFailure, T> = TestFailureEvent(DownstreamFailure(
            reason = "$prefix$reason$suffix"
        ))
    }

    private class ResultUnexpectedMapping<X : Any, T : Any, S : Any>
    : MappingEvent.ResultMapping<X, T, S> {
        override fun map(result: S): Event<X, T> {
            fail("Result unexpected: $result")
        }
    }

    private class FailureUnexpectedMapping<X : Any, T : Any, Y : Any>
    : MappingEvent.FailureMapping<X, T, Y> {
        override fun map(reason: Y): Event<X, T> {
            fail("Failure unexpected: $reason")
        }
    }

    //region mapEvent
    @Test fun `mapEvent maps result`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Result(
            value = UpstreamResult("Slartibartfast")
        )
        val resultEvent: Event<DownstreamFailure, DownstreamResult> = upstreamEvent.mapEvent(
            failureMapping = FailureUnexpectedMapping(),
            resultMapping = ResultMapping("Mapped: ", "!")
        )

        val mock = TestResultEvent.Delegate<DownstreamFailure, DownstreamResult>(
            expectedResult = DownstreamResult("Mapped: UpstreamResult(name=Slartibartfast)!")
        )
        resultEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapEvent maps failure`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Failure(
            reason = UpstreamFailure("Fuel not found")
        )
        val failureEvent: Event<DownstreamFailure, DownstreamResult> = upstreamEvent.mapEvent(
            failureMapping = FailureMapping("ERROR: ", "?!"),
            resultMapping = ResultUnexpectedMapping()
        )

        val mock = TestFailureEvent.Delegate<DownstreamFailure, DownstreamResult>(
            expectedReason = DownstreamFailure("ERROR: UpstreamFailure(reason=Fuel not found)?!")
        )
        failureEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapEvent respects result identity`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Result(
            value = UpstreamResult("Slartibartfast")
        )
        val resultEvent: Event<UpstreamFailure, UpstreamResult> = upstreamEvent.mapEvent(
            failureMapping = FailureUnexpectedMapping(),
            resultMapping = MappingEvent.ResultMapping.Identity()
        )

        val mock = TestResultEvent.Delegate<UpstreamFailure, UpstreamResult>(
            expectedResult = UpstreamResult(name = "Slartibartfast")
        )
        resultEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapEvent respects failure identity`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Failure(
            reason = UpstreamFailure("Fuel not found")
        )
        val failureEvent: Event<UpstreamFailure, UpstreamResult> = upstreamEvent.mapEvent(
            failureMapping = MappingEvent.FailureMapping.Identity(),
            resultMapping = ResultUnexpectedMapping()
        )

        val mock = TestFailureEvent.Delegate<UpstreamFailure, UpstreamResult>(
            expectedReason = UpstreamFailure(reason = "Fuel not found")
        )
        failureEvent.evaluate(mock)
        mock.verify()
    }
    //endregion

    //region mapResult
    @Test fun `mapResult maps result`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Result(
            value = UpstreamResult("Slartibartfast")
        )
        val resultEvent: Event<UpstreamFailure, DownstreamResult> = upstreamEvent.mapResult(
            mapping = ResultMapping("Mapped: ", "!")
        )

        val mock = TestResultEvent.Delegate<UpstreamFailure, DownstreamResult>(
            expectedResult = DownstreamResult("Mapped: UpstreamResult(name=Slartibartfast)!")
        )
        resultEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapResult forwards failure`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Failure(
            reason = UpstreamFailure("Bonk!")
        )
        val resultEvent: Event<UpstreamFailure, DownstreamResult> = upstreamEvent.mapResult(
            mapping = ResultMapping("Mapped: ", "!")
        )

        val mock = TestFailureEvent.Delegate<UpstreamFailure, DownstreamResult>(
            expectedReason = UpstreamFailure("Bonk!")
        )
        resultEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapResult accepts block`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Result(
            value = UpstreamResult("Slartibartfast")
        )
        val resultEvent = upstreamEvent.mapResult {
            TestResultEvent<UpstreamFailure, DownstreamResult>(DownstreamResult(
                message = "Hello ${it.name}!"
            ))
        }

        val mock = TestResultEvent.Delegate<UpstreamFailure, DownstreamResult>(
            expectedResult = DownstreamResult("Hello Slartibartfast!")
        )
        resultEvent.evaluate(mock)
        mock.verify()
    }
    //endregion

    //region mapFailure
    @Test fun `mapFailure maps failure`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Failure(
            reason = UpstreamFailure("Kapuuut")
        )
        val failureEvent: Event<DownstreamFailure, UpstreamResult> = upstreamEvent.mapFailure(
            mapping = FailureMapping("Got: ", "!")
        )

        val mock = TestFailureEvent.Delegate<DownstreamFailure, UpstreamResult>(
            expectedReason = DownstreamFailure("Got: UpstreamFailure(reason=Kapuuut)!")
        )
        failureEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapFailure forwards result`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Result(
            value = UpstreamResult("Meh")
        )
        val failureEvent: Event<DownstreamFailure, UpstreamResult> = upstreamEvent.mapFailure(
            mapping = FailureMapping("Got: ", "!")
        )

        val mock = TestResultEvent.Delegate<DownstreamFailure, UpstreamResult>(
            expectedResult = UpstreamResult(name = "Meh")
        )
        failureEvent.evaluate(mock)
        mock.verify()
    }

    @Test fun `mapFailure accepts block`() {
        val upstreamEvent: Event<UpstreamFailure, UpstreamResult> = Event.Failure(
            reason = UpstreamFailure("Kapuuut")
        )
        val failureEvent = upstreamEvent.mapFailure {
            TestFailureEvent<DownstreamFailure, UpstreamResult>(DownstreamFailure(
                reason = "Got: ${it.reason}!"
            ))
        }

        val mock = TestFailureEvent.Delegate<DownstreamFailure, UpstreamResult>(
            expectedReason = DownstreamFailure("Got: Kapuuut!")
        )
        failureEvent.evaluate(mock)
        mock.verify()
    }
    //endregion
}
