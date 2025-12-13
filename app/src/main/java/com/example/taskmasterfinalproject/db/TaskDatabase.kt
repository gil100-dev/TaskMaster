package com.example.taskmasterfinalproject.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskmasterfinalproject.data.TaskDao
import com.example.taskmasterfinalproject.model.Subtask
import com.example.taskmasterfinalproject.model.Tag
import com.example.taskmasterfinalproject.model.Task
import com.example.taskmasterfinalproject.model.TaskTagCrossRef

@Database(
    entities = [Task::class, Subtask::class, Tag::class, TaskTagCrossRef::class],
    version = 2,
    exportSchema = true // It's good practice to export schema for testing migrations
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add createdAt and completedAt columns to tasks table
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `completedAt` INTEGER")

                // Create new tables
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subtasks` (
                        `id` TEXT NOT NULL, 
                        `taskId` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `isDone` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `colorHex` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `TaskTagCrossRef` (
                        `taskId` TEXT NOT NULL, 
                        `tagId` TEXT NOT NULL, 
                        PRIMARY KEY(`taskId`, `tagId`)
                    )
                """.trimIndent())

                // Create indices for better query performance
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subtasks_taskId` ON `subtasks` (`taskId`)")
            }
        }

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
