package com.example.taskmasterfinalproject.db

// ספרייה לגישה ל-Context של האפליקציה (נדרש לבניית מסד הנתונים)
import android.content.Context
// ספרייה להגדרת מסד הנתונים – האנוטציה @Database מגדירה אילו ישויות (טבלאות) כלולות
import androidx.room.Database
// ספרייה לבניית מסד הנתונים באמצעות דפוס Builder
import androidx.room.Room
// מחלקת בסיס אבסטרקטית למסד הנתונים של Room
import androidx.room.RoomDatabase
// ספרייה לטיפול במיגרציות – שינויי סכמה בין גרסאות מסד הנתונים
import androidx.room.migration.Migration
// ספרייה לתמיכה בגישה ישירה ל-SQLite (נדרשת למיגרציות שמבצעות ALTER TABLE)
import androidx.sqlite.db.SupportSQLiteDatabase
// ממשק ה-DAO עבור המשימות
import com.example.taskmasterfinalproject.data.TaskDao
// ייבוא המודלים (ישויות = טבלאות במסד הנתונים)
import com.example.taskmasterfinalproject.model.Subtask
import com.example.taskmasterfinalproject.model.Tag
import com.example.taskmasterfinalproject.model.Task
import com.example.taskmasterfinalproject.model.TaskTagCrossRef

// הגדרת מסד הנתונים עם @Database:
// entities – רשימת הישויות (טבלאות) שיווצרו
// version – גרסת הסכמה הנוכחית (חייבת לעלות בכל שינוי מבנה)
// exportSchema – האם לייצא סכמה לקובץ JSON (שימושי לבדיקות)
@Database(
    entities = [Task::class, Subtask::class, Tag::class, TaskTagCrossRef::class, com.example.taskmasterfinalproject.model.User::class],
    version = 5,
    exportSchema = true 
)
abstract class TaskDatabase : RoomDatabase() {

    // פונקציה מופשטת לקבלת ה-DAO של משימות – Room מייצרת את המימוש אוטומטית
    abstract fun taskDao(): TaskDao
    // פונקציה מופשטת לקבלת ה-DAO של משתמשים
    abstract fun userDao(): UserDao

    // אובייקט נלווה ליישום דפוס Singleton – מבטיח מופע יחיד של מסד הנתונים באפליקציה
    companion object {
        // @Volatile מבטיח שהערך תמיד נקרא מהזיכרון הראשי ולא מ-Cache של thread מסוים
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // מיגרציה מגרסה 1 ל-2: הוספת עמודות createdAt, completedAt, וטבלאות subtasks, tags, TaskTagCrossRef
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // הוספת עמודת זמן יצירה לטבלת המשימות
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                // הוספת עמודת זמן השלמה
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `completedAt` INTEGER")
                // יצירת טבלת תת-משימות עם מפתח זר למשימה אב (CASCADE למחיקה)
                db.execSQL("CREATE TABLE IF NOT EXISTS `subtasks` (`id` TEXT NOT NULL, `taskId` TEXT NOT NULL, `title` TEXT NOT NULL, `isDone` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                // יצירת טבלת תגיות
                db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, PRIMARY KEY(`id`))")
                // יצירת טבלת קישור Many-to-Many בין משימות לתגיות
                db.execSQL("CREATE TABLE IF NOT EXISTS `TaskTagCrossRef` (`taskId` TEXT NOT NULL, `tagId` TEXT NOT NULL, PRIMARY KEY(`taskId`, `tagId`))")
                // יצירת אינדקס על taskId לשיפור ביצועי שליפה
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subtasks_taskId` ON `subtasks` (`taskId`)")
            }
        }

        // מיגרציה מגרסה 2 ל-3: הוספת אינדקס חסר על tagId בטבלת הקישור
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_TaskTagCrossRef_tagId` ON `TaskTagCrossRef` (`tagId`)")
            }
        }

        // מיגרציה מגרסה 3 ל-4: הוספת טבלת משתמשים עם אינדקס ייחודי על שם המשתמש
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // יצירת טבלת users עם Auto Increment על ה-ID
                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `salt` TEXT NOT NULL)")
                // אינדקס ייחודי – מונע רישום שני משתמשים עם אותו שם
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)")
            }
        }

        // מיגרציה מגרסה 4 ל-5: הוספת עמודת ownerUserId לטבלאות tasks ו-tags (בידוד נתונים לכל משתמש)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // הוספת שיוך משתמש למשימות (-1 = ליגאסי/לא משויך)
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `ownerUserId` INTEGER NOT NULL DEFAULT -1")
                // הוספת שיוך משתמש לתגיות
                db.execSQL("ALTER TABLE `tags` ADD COLUMN `ownerUserId` INTEGER NOT NULL DEFAULT -1")
            }
        }

        // פונקציה לקבלת המופע היחיד (Singleton) של מסד הנתונים.
        // synchronized מבטיח ש-thread אחד בלבד יכול ליצור את ה-Instance – מונע יצירה כפולה
        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,        // שימוש ב-applicationContext למניעת דליפות זיכרון
                    TaskDatabase::class.java,
                    "task_database"                    // שם קובץ מסד הנתונים
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)  // רישום כל המיגרציות
                .fallbackToDestructiveMigration()     // רשת ביטחון: אם אין מיגרציה מתאימה – מוחק ובונה מחדש
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
