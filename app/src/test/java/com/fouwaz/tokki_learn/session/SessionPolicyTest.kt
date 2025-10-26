package com.fouwaz.tokki_learn.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SessionPolicyTest {

    @Test
    fun `should show gate when cooldown expired`() {
        val policy = SessionPolicy { 15 }
        val now = 0L

        assertTrue(policy.shouldShowGate(TEST_PACKAGE, now))

        policy.markGateShown(TEST_PACKAGE, now)
        assertFalse(policy.shouldShowGate(TEST_PACKAGE, now + TimeUnit.MINUTES.toMillis(10)))
        assertTrue(policy.shouldShowGate(TEST_PACKAGE, now + TimeUnit.MINUTES.toMillis(16)))
    }

    @Test
    fun `active gate blocks duplicate triggers`() {
        val policy = SessionPolicy { 15 }
        policy.markGateActive(TEST_PACKAGE)
        assertFalse(policy.shouldShowGate(TEST_PACKAGE, 0L))
        policy.markGateInactive(TEST_PACKAGE)
        assertTrue(policy.shouldShowGate(TEST_PACKAGE, 0L))
    }

    private companion object {
        const val TEST_PACKAGE = "com.example.app"
    }
}
