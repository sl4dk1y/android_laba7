package com.example.laba7.data

import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class UserDaoInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var authLogDao: AuthLogDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.userDao()
        authLogDao = database.authLogDao()
        repository = UserRepository(dao, authLogDao)
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun usersAndRolesAreStoredAndObserved() = runBlocking {
        val adminId = dao.insert(User(login = "admin", passwordHash = "hash", fullName = "Admin",
            birthDate = "01.01.2000", gender = "Мужской", avatarUri = null, isAdmin = true))
        val userId = dao.insert(User(login = "user", passwordHash = "hash", fullName = "User",
            birthDate = "02.02.2000", gender = "Женский", avatarUri = null, isAdmin = false))

        val admin = dao.findById(adminId)
        assertTrue(admin!!.isAdmin)
        dao.update(dao.findById(userId)!!.copy(isAdmin = true))
        assertTrue(dao.findById(userId)!!.isAdmin)
        assertEquals(2, dao.observeAll().first().size)
    }

    @Test
    fun duplicateLoginIsRejected() {
        runBlocking {
            val first = User(login = "same", passwordHash = "hash", fullName = "First",
                birthDate = "01.01.2000", gender = "Мужской", avatarUri = null, isAdmin = true)
            dao.insert(first)
            assertThrows(SQLiteConstraintException::class.java) { runBlocking { dao.insert(first.copy(id = 0)) } }
        }
    }

    @Test
    fun themePreferenceCanBeReadAfterWriting() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "lab5_instrumented_test"
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putBoolean("dark_theme", true).commit()
        assertTrue(context.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean("dark_theme", false))
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        assertFalse(context.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean("dark_theme", false))
    }

    @Test
    fun successfulAuthenticationAddsLogButFailureDoesNot() = runBlocking {
        repository.register("logtest", "pass1234", "Test User", "01.01.2000", "Мужской", null).getOrThrow()

        assertEquals(null, repository.authenticate("logtest", "wrong-password"))
        assertTrue(authLogDao.observeAllNewestFirst().first().isEmpty())

        assertEquals("logtest", repository.authenticate("logtest", "pass1234")?.login)
        assertEquals(listOf("logtest"), authLogDao.observeAllNewestFirst().first().map { it.login })
    }

    @Test
    fun authLogIsEmptyAndSortedByTimestampThenIdDescending() = runBlocking {
        assertTrue(authLogDao.observeAllNewestFirst().first().isEmpty())
        authLogDao.insert(AuthLog(userId = null, login = "older", timestamp = 100L))
        authLogDao.insert(AuthLog(userId = null, login = "newer", timestamp = 200L))
        authLogDao.insert(AuthLog(userId = null, login = "same-time-last", timestamp = 200L))

        assertEquals(listOf("same-time-last", "newer", "older"), authLogDao.observeAllNewestFirst().first().map { it.login })
    }

    @Test
    fun legacyImportIsStrictAndRepeatable() = runBlocking {
        val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        val legacyFile = File(testContext.cacheDir, "legacy-auth-test.log")
        legacyFile.writeText("23.10.2026 10:20:30 — logtest\nnot a valid entry\n")
        val contextWithLegacyLog = object : ContextWrapper(testContext) {
            override fun openFileInput(name: String): FileInputStream =
                if (name == "auth.log") FileInputStream(legacyFile) else super.openFileInput(name)
        }
        try {
            assertEquals(1, repository.importLegacyAuthLog(contextWithLegacyLog))
            assertEquals(0, repository.importLegacyAuthLog(contextWithLegacyLog))
            assertEquals(listOf("logtest"), authLogDao.observeAllNewestFirst().first().map { it.login })
        } finally {
            legacyFile.delete()
        }
    }

    @Test
    fun migrationAddsAuthLogTableAndPreservesUsers() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration_${System.nanoTime()}.db"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        val oldDb = SQLiteDatabase.openOrCreateDatabase(file, null)
        oldDb.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `login` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `fullName` TEXT NOT NULL, `birthDate` TEXT NOT NULL, `gender` TEXT NOT NULL, `avatarUri` TEXT, `isAdmin` INTEGER NOT NULL)")
        oldDb.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_login` ON `users` (`login`)")
        oldDb.execSQL("INSERT INTO `users` (`id`, `login`, `passwordHash`, `fullName`, `birthDate`, `gender`, `avatarUri`, `isAdmin`) VALUES (1, 'preserved', 'not-a-real-secret', 'Preserved User', '01.01.2000', 'Мужской', NULL, 1)")
        oldDb.version = 1
        oldDb.close()

        var migrated: AppDatabase? = null
        try {
            migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()
            runBlocking {
                assertEquals("preserved", migrated!!.userDao().findByLogin("preserved")?.login)
                assertTrue(migrated!!.authLogDao().observeAllNewestFirst().first().isEmpty())
            }
        } finally {
            migrated?.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun onlyAdministratorsCanAccessAdministrativeScreens() {
        assertFalse(com.example.laba7.canAccessAdminScreens(null))
        assertFalse(com.example.laba7.canAccessAdminScreens(User(id = 1, login = "ordinary", passwordHash = "", fullName = "", birthDate = "", gender = "", avatarUri = null, isAdmin = false)))
        assertTrue(com.example.laba7.canAccessAdminScreens(User(id = 2, login = "admin", passwordHash = "", fullName = "", birthDate = "", gender = "", avatarUri = null, isAdmin = true)))
    }

    @Test
    fun databaseDelayCanBeEnabledAndPersistsWithoutBlocking() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("database_delay_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val controller = DatabaseDelayController(prefs)
        assertFalse(controller.enabled.first())

        controller.setEnabled(true)
        assertTrue(DatabaseDelayController(prefs).enabled.first())
        val startedAt = System.nanoTime()
        controller.waitIfEnabled()
        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000
        assertTrue("Expected approximately a three-second suspend delay, got ${elapsedMillis}ms", elapsedMillis >= 2_900)

        controller.setEnabled(false)
        val immediateStartedAt = System.nanoTime()
        controller.waitIfEnabled()
        assertTrue((System.nanoTime() - immediateStartedAt) / 1_000_000 < 500)
        prefs.edit().clear().commit()
        Unit
    }
}
