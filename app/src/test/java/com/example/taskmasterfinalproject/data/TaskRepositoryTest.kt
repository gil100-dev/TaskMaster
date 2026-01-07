package com.example.taskmasterfinalproject.data

import com.example.taskmasterfinalproject.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class TaskRepositoryTest {

    // Helper to create a dummy repository (we only test the static-like method calculateTaskScore)
    // Since calculateTaskScore is an instance method (though effectively pure), we need an instance.
    // We can mock the DAO since we won't call it.
    private val mockDao = Mockito.mock(TaskDao::class.java)
    private val repository = TaskRepository(mockDao)

    private val FIXED_NOW = 1000000000000L // Arbitrary fixed time
    private val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

    @Test
    fun calculateScore_highPriority_shouldScoreHigher() {
        val highTask = createTask(priority = 1) // High
        val medTask = createTask(priority = 2) // Medium
        val lowTask = createTask(priority = 3) // Low

        val highScore = repository.calculateTaskScore(highTask, FIXED_NOW)
        val medScore = repository.calculateTaskScore(medTask, FIXED_NOW)
        val lowScore = repository.calculateTaskScore(lowTask, FIXED_NOW)

        // 100 vs 50 vs 10
        assertTrue("High priority should outscore Medium", highScore > medScore)
        assertTrue("Medium priority should outscore Low", medScore > lowScore)
        assertEquals(100, highScore)
    }

    @Test
    fun calculateScore_overdue_shouldBeCritical() {
        // Due yesterday
        val overdueTask = createTask(dueTime = FIXED_NOW - ONE_DAY_MILLIS)
        
        val score = repository.calculateTaskScore(overdueTask, FIXED_NOW)
        
        // 500 (Overdue) + 0 (No priority set)
        assertEquals(500, score)
    }

    @Test
    fun calculateScore_dueToday_shouldBeUrgent() {
        // Due in 1 hour
        val dueTodayTask = createTask(dueTime = FIXED_NOW + 3600000)

        val score = repository.calculateTaskScore(dueTodayTask, FIXED_NOW)

        // 200 (Due Today)
        assertEquals(200, score)
    }

    @Test
    fun calculateScore_smartLogic_Integration() {
        // Scenario: Low Priority Due Today vs High Priority Due Next Week
        val urgentLow = createTask(priority = 3, dueTime = FIXED_NOW + 3600000) // (10 + 200) = 210
        val distantHigh = createTask(priority = 1, dueTime = FIXED_NOW + (10 * ONE_DAY_MILLIS)) // (100 + 0) = 100

        val scoreUrgent = repository.calculateTaskScore(urgentLow, FIXED_NOW)
        val scoreDistant = repository.calculateTaskScore(distantHigh, FIXED_NOW)

        assertTrue("Urgent (Due Today) should rank higher than Distant High Priority", scoreUrgent > scoreDistant)
    }

    @Test
    fun calculateScore_completed_shouldBeLowest() {
        val completedHigh = createTask(priority = 1, isCompleted = true)
        val pendingLow = createTask(priority = 3, isCompleted = false)

        val scoreCompleted = repository.calculateTaskScore(completedHigh, FIXED_NOW)
        val scorePending = repository.calculateTaskScore(pendingLow, FIXED_NOW)

        assertTrue(scorePending > scoreCompleted)
        assertEquals(-1000, scoreCompleted)
    }

    private fun createTask(
        priority: Int = 0,
        dueTime: Long? = null,
        isCompleted: Boolean = false
    ): Task {
        return Task(
            id = "test_id",
            title = "Test Task",
            description = null,
            dueDate = null,
            priority = priority,
            dueTimeMillis = dueTime,
            isCompleted = isCompleted
        )
    }
}
