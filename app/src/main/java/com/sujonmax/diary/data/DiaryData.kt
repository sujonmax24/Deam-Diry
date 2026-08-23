package com.sujonmax.diary.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes", indices = [Index("updatedAt"), Index("folderId")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val contentFormat: String = "plain",
    val attachmentUris: String = "",
    val folderId: Long? = null,
    val mood: String? = null,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders", indices = [Index(value = ["name"], unique = true)])
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (:query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun observeNotes(query: String): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun observeTrash(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): NoteEntity?
    @Insert suspend fun insert(note: NoteEntity): Long
    @Update suspend fun update(note: NoteEntity)
    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun moveToTrash(id: Long, now: Long = System.currentTimeMillis())
    @Query("UPDATE notes SET isDeleted = 0, updatedAt = :now WHERE id = :id")
    suspend fun restore(id: Long, now: Long = System.currentTimeMillis())
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteForever(id: Long)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<FolderEntity>>
    @Insert suspend fun insert(folder: FolderEntity): Long
    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [NoteEntity::class, FolderEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    companion object {
        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context, AppDatabase::class.java, "diary_database"
        ).addMigrations(MIGRATION_1_2).build()
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN contentFormat TEXT NOT NULL DEFAULT 'plain'")
                db.execSQL("ALTER TABLE notes ADD COLUMN attachmentUris TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN mood TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

class NoteRepository(private val notes: NoteDao, private val folders: FolderDao) {
    fun observeNotes(query: String) = notes.observeNotes(query)
    val trash = notes.observeTrash()
    val allFolders = folders.observeAll()
    suspend fun find(id: Long) = notes.find(id)
    suspend fun save(note: NoteEntity) = if (note.id == 0L) notes.insert(note) else { notes.update(note.copy(updatedAt = System.currentTimeMillis())); note.id }
    suspend fun trash(id: Long) = notes.moveToTrash(id)
    suspend fun restore(id: Long) = notes.restore(id)
    suspend fun deleteForever(id: Long) = notes.deleteForever(id)
    suspend fun createFolder(name: String) = folders.insert(FolderEntity(name = name.trim()))
    suspend fun deleteFolder(id: Long) = folders.delete(id)
}