package com.example.taskmasterfinalproject.data

// ייבוא מודל המשימה לצורך יצירת אובייקטי בדיקה
import com.example.taskmasterfinalproject.model.Task
// ספרייה לבדיקת שוויון בין ערך צפוי לבין ערך בפועל – assertEquals(expected, actual)
import org.junit.Assert.assertEquals
// ספרייה לבדיקת תנאי (שהביטוי true) – assertTrue(condition)
import org.junit.Assert.assertTrue
// אנוטציה המסמנת פונקציה כבדיקה יחידה (Unit Test) – JUnit ירוץ עליה אוטומטית
import org.junit.Test
// ספרייית Mockito – מאפשרת ליצור "מוקים" (אובייקטים מזויפים) לצורך בדיקות
import org.mockito.Mockito

// מחלקת בדיקות יחידה (Unit Tests) עבור TaskRepository.
// בודקת את פונקציית calculateTaskScore – הלוגיקה של "מיון חכם" (Smart Sort)
class TaskRepositoryTest {

    // יצירת Mock של ה-DAO – אובייקט מזויף שלא ניגש באמת למסד הנתונים.
    // Mockito.mock() יוצר מופע שמחזיר ערכי ברירת מחדל (null/0) לכל קריאה
    private val mockDao = Mockito.mock(TaskDao::class.java)
    // יצירת ה-Repository עם ה-Mock – כך הבדיקות לא תלויות במסד נתונים אמיתי
    private val repository = TaskRepository(mockDao)

    // קבועים לצורך הבדיקות
    private val FIXED_NOW = 1000000000000L    // זמן קבוע (מילישניות) – מונע תלות בזמן אמיתי
    private val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L  // יום אחד במילישניות

    // בדיקה: משימה בעדיפות גבוהה צריכה לקבל ציון גבוה יותר ממשימה בעדיפות נמוכה
    @Test
    fun calculateScore_highPriority_shouldScoreHigher() {
        val highTask = createTask(priority = 1)  // עדיפות גבוהה
        val medTask = createTask(priority = 2)   // עדיפות בינונית
        val lowTask = createTask(priority = 3)   // עדיפות נמוכה

        // חישוב הציון לכל משימה
        val highScore = repository.calculateTaskScore(highTask, FIXED_NOW)
        val medScore = repository.calculateTaskScore(medTask, FIXED_NOW)
        val lowScore = repository.calculateTaskScore(lowTask, FIXED_NOW)

        // וידוא שהציונים בסדר יורד: גבוהה > בינונית > נמוכה
        assertTrue("High priority should outscore Medium", highScore > medScore)
        assertTrue("Medium priority should outscore Low", medScore > lowScore)
        assertEquals(100, highScore)  // ציון צפוי לעדיפות גבוהה: 100
    }

    // בדיקה: משימה שעברה את הדדליין צריכה לקבל ציון קריטי (500)
    @Test
    fun calculateScore_overdue_shouldBeCritical() {
        // משימה שהיתה אמורה להסתיים אתמול
        val overdueTask = createTask(dueTime = FIXED_NOW - ONE_DAY_MILLIS)
        
        val score = repository.calculateTaskScore(overdueTask, FIXED_NOW)
        
        // 500 (באיחור) + 0 (ללא עדיפות) = 500
        assertEquals(500, score)
    }

    // בדיקה: משימה שהדדליין שלה היום צריכה לקבל דחיפות גבוהה (200)
    @Test
    fun calculateScore_dueToday_shouldBeUrgent() {
        // משימה שמגיעה בעוד שעה
        val dueTodayTask = createTask(dueTime = FIXED_NOW + 3600000)

        val score = repository.calculateTaskScore(dueTodayTask, FIXED_NOW)

        // 200 (היום)
        assertEquals(200, score)
    }

    // בדיקת אינטגרציה של המיון החכם: דחיפות > עדיפות
    @Test
    fun calculateScore_smartLogic_Integration() {
        // תרחיש: משימה בעדיפות נמוכה שמגיעה היום לעומת משימה בעדיפות גבוהה שמגיעה בעוד 10 ימים
        val urgentLow = createTask(priority = 3, dueTime = FIXED_NOW + 3600000)           // (10 + 200) = 210
        val distantHigh = createTask(priority = 1, dueTime = FIXED_NOW + (10 * ONE_DAY_MILLIS)) // (100 + 0) = 100

        val scoreUrgent = repository.calculateTaskScore(urgentLow, FIXED_NOW)
        val scoreDistant = repository.calculateTaskScore(distantHigh, FIXED_NOW)

        // משימה דחופה (גם בעדיפות נמוכה) צריכה לדרג גבוה יותר ממשימה חשובה אך רחוקה
        assertTrue("Urgent (Due Today) should rank higher than Distant High Priority", scoreUrgent > scoreDistant)
    }

    // בדיקה: משימה שהושלמה צריכה לקבל את הציון הנמוך ביותר (-1000)
    @Test
    fun calculateScore_completed_shouldBeLowest() {
        val completedHigh = createTask(priority = 1, isCompleted = true)   // הושלמה
        val pendingLow = createTask(priority = 3, isCompleted = false)     // פתוחה

        val scoreCompleted = repository.calculateTaskScore(completedHigh, FIXED_NOW)
        val scorePending = repository.calculateTaskScore(pendingLow, FIXED_NOW)

        // משימה פתוחה תמיד גבוהה ממשימה שהושלמה
        assertTrue(scorePending > scoreCompleted)
        assertEquals(-1000, scoreCompleted)  // ציון קבוע למשימות שהושלמו
    }

    // פונקציית עזר ליצירת אובייקט משימה לבדיקות.
    // מאפשרת ליצור משימה עם פרמטרים ספציפיים ובררות מחדל לשאר
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
