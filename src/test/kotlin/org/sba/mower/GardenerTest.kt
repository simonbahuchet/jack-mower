package org.sba.mower

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.sba.mower.helper.mockInputCollocation
import org.sba.mower.helper.mockInputOffField
import org.sba.mower.helper.readInputs
import org.sba.mower.helper.readResults

@OptIn(ExperimentalCoroutinesApi::class)
class GardenerTest {

    @Test
    fun `Test wrong inputs - collocation`() {

        // Given
        val inputs = mockInputCollocation()

        // When
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val gardener = Gardener(this)
                gardener.mow(inputs)
            }
        }
        Assertions.assertTrue(exception.message!!.contains("There can't be 2 mowers sharing the same location"))
    }

    @Test
    fun `Test wrong inputs - start off field`() {

        // Given
        val inputs = mockInputOffField()

        // When
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val gardener = Gardener(this)
                gardener.mow(inputs)
            }
        }
        Assertions.assertTrue(exception.message!!.contains("The initial coordinates of the mower cannot be off field"))
    }

    @Test
    fun `Test nominal case`() {
        runTestBasedOnFiles("nominal")
    }

    @Test
    fun `Test "move off field" scenario`() {
        runTestBasedOnFiles("go-off-field")
    }

    @Test
    fun `Test "occupied cell" scenario`() {
        runTestBasedOnFiles("occupied-cell")
    }

    /**
     * TODO: cannot use runBlockingTest for now. Or I get a:
     *  java.lang.IllegalStateException: This job has not completed yet
     *  It seems to be an bug cf. https://github.com/Kotlin/kotlinx.coroutines/issues/1222
     */
    private fun runTestBasedOnFiles(scenarioName: String) = runBlocking {
        // Given
        val inputs = readInputs("/inputs-$scenarioName.txt")

        // When
        val gardener = Gardener(this)
        val result = gardener.mow(inputs)

        // Then
        val expectedResults = readResults("/results-$scenarioName.txt").positions
        for (i in result.indices) {
            Assertions.assertEquals(expectedResults[i], result[i])
        }
    }

    @Test
    fun testDelayInSuspend() = runBlockingTest {
        // Given
        val realStartTime = System.currentTimeMillis()
        val virtualStartTime = currentTime

        // When
        delayedFoo()

        // Then
        println("Current time: ${System.currentTimeMillis() - realStartTime} ms")  // ~ 6 ms
        println("Virtual time: ${currentTime - virtualStartTime} ms")              // 1000 ms
    }

    suspend fun delayedFoo() {
        delay(1000) // auto-advances without delay
        println("foo")       // executes eagerly when foo() is called
    }
}


