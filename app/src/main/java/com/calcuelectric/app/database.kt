package com.calcuelectric.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val operationsCount: Int = 0
)

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int, // Remote ID
    val userId: Int, // Relación con el usuario
    val label: String,
    val result: String,
    val formulaLabel: String,
    val studentName: String? = null,
    val createdAt: String? = null
)

@Dao
interface AppDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("SELECT * FROM operations ORDER BY id DESC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE userId = :userId ORDER BY id DESC")
    fun getOperationsByUser(userId: Int): Flow<List<OperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperations(operations: List<OperationEntity>)

    @Query("DELETE FROM operations WHERE id = :id")
    suspend fun deleteOperationById(id: Int)

    @Query("DELETE FROM operations")
    suspend fun clearOperations()
    
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Database(entities = [UserEntity::class, OperationEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calcuelectric_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
