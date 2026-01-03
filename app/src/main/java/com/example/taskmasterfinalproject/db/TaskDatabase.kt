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
    entities = [Task::class, Subtask::class, Tag::class, TaskTagCrossRef::class, com.example.taskmasterfinalproject.model.User::class],
    version = 4,
    exportSchema = true 
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `completedAt` INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS `subtasks` (`id` TEXT NOT NULL, `taskId` TEXT NOT NULL, `title` TEXT NOT NULL, `isDone` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `TaskTagCrossRef` (`taskId` TEXT NOT NULL, `tagId` TEXT NOT NULL, PRIMARY KEY(`taskId`, `tagId`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subtasks_taskId` ON `subtasks` (`taskId`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_TaskTagCrossRef_tagId` ON `TaskTagCrossRef` (`tagId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `salt` TEXT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)")
            }
        }

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration() // Safety net
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
