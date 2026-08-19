package com.dwightsckrute.visto.data.local

import android.content.Context
import java.io.File
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dwightsckrute.visto.data.local.dao.WatchlistDao
import com.dwightsckrute.visto.data.local.entity.WatchlistItemEntity
import com.dwightsckrute.visto.data.local.converters.GenreIdsConverter
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dwightsckrute.visto.data.local.converters.InstantConverters
import com.dwightsckrute.visto.data.local.converters.LongListConverter
import com.dwightsckrute.visto.data.local.converters.MediaListsIconConverter
import com.dwightsckrute.visto.data.local.converters.SeasonNameConverter
import com.dwightsckrute.visto.data.local.converters.WatchStatusConverter
import com.dwightsckrute.visto.data.local.dao.CustomListsDao
import com.dwightsckrute.visto.data.local.dao.MovieBundleDao
import com.dwightsckrute.visto.data.local.dao.SeasonDao
import com.dwightsckrute.visto.data.local.dao.TrendingDao
import com.dwightsckrute.visto.data.local.dao.TvBundleDao
import com.dwightsckrute.visto.data.local.dao.TvEpisodeDao
import com.dwightsckrute.visto.data.local.entity.CustomListEntity
import com.dwightsckrute.visto.data.local.entity.MovieBundleEntity
import com.dwightsckrute.visto.data.local.entity.SeasonEntity
import com.dwightsckrute.visto.data.local.entity.TrendingEntity
import com.dwightsckrute.visto.data.local.entity.TvBundleEntity
import com.dwightsckrute.visto.data.local.entity.TvEpisodeEntity

@Database(
    entities = [WatchlistItemEntity::class, MovieBundleEntity::class, TvBundleEntity::class, SeasonEntity::class, TvEpisodeEntity::class, CustomListEntity::class, TrendingEntity::class],
    version = 35
)
@TypeConverters(
    GenreIdsConverter::class,
    InstantConverters::class,
    WatchStatusConverter::class,
    SeasonNameConverter::class,
    LongListConverter::class,
    MediaListsIconConverter::class
)
abstract class VistoDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao
    abstract fun movieBundleDao(): MovieBundleDao

    abstract fun tvBundleDao(): TvBundleDao

    abstract fun seasonDao(): SeasonDao

    abstract fun tvEpisodeDao(): TvEpisodeDao

    abstract fun movieListsDao(): CustomListsDao

    abstract fun trendingDao(): TrendingDao

    companion object {
        private const val DATABASE_NAME = "visto.db"

        /** Como se llamaba el fichero cuando la aplicación se llamaba WatchMaster. */
        private const val LEGACY_DATABASE_NAME = "watchmaster.db"

        @Volatile
        private var INSTANCE: VistoDatabase? = null

        /**
         * Lleva la base de datos de su nombre antiguo al nuevo, si hace falta.
         *
         * El fichero es lo único del cambio de nombre que no se puede tocar y ya está: dentro hay
         * una biblioteca entera, y abrir Room con otro nombre habría creado una vacía al lado
         * dejando la de verdad ahí olvidada. Renombrar el fichero es instantáneo y conserva todo,
         * incluidas las migraciones de esquema, porque el contenido no se toca.
         *
         * Van también `-wal` y `-shm`: son el diario de escritura de SQLite y quedarse sin ellos
         * puede costar lo último que hubiera sin volcar.
         *
         * Solo actúa si el nuevo no existe todavía. Si existe, esto ya pasó.
         */
        private fun renameLegacyFile(context: Context) {
            val target = context.getDatabasePath(DATABASE_NAME)
            if (target.exists()) return

            val legacy = context.getDatabasePath(LEGACY_DATABASE_NAME)
            if (!legacy.exists()) return

            for (suffix in listOf("", "-wal", "-shm")) {
                val from = File(legacy.path + suffix)
                if (from.exists()) from.renameTo(File(target.path + suffix))
            }
        }

        fun getInstance(context: Context): VistoDatabase {
            return INSTANCE ?: synchronized(this) {
                renameLegacyFile(context.applicationContext)
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VistoDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32,
                    MIGRATION_32_33,
                    MIGRATION_33_34,
                    MIGRATION_34_35
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS movie_lists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                icon INTEGER,
                movieIds TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
        )
    }
}


val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE movie_lists_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                icon INTEGER,
                movieIds TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO movie_lists_new (id, name, description, icon, movieIds)
            SELECT id, name, 
                   COALESCE(description, ''), 
                   icon, 
                   movieIds
            FROM movie_lists
        """.trimIndent()
        )

        db.execSQL("DROP TABLE movie_lists")
        db.execSQL("ALTER TABLE movie_lists_new RENAME TO movie_lists")
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE movie_lists_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                icon TEXT,
                movieIds TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO movie_lists_new (id, name, description, icon, movieIds)
            SELECT id, name, 
                   description, 
                   icon, 
                   movieIds
            FROM movie_lists
        """.trimIndent()
        )

        db.execSQL("DROP TABLE movie_lists")
        db.execSQL("ALTER TABLE movie_lists_new RENAME TO movie_lists")
    }
}

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE movie_lists ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )
    }
}

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE tv_seasons ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )
    }
}

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE movie_lists RENAME TO custom_lists
        """.trimIndent()
        )
    }
}

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE custom_lists_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                icon TEXT,
                ids TEXT NOT NULL,
                isPinned INTEGER NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO custom_lists_new (id, name, description, icon, ids, isPinned)
            SELECT id, name, description, icon, movieIds, isPinned
            FROM custom_lists
        """.trimIndent()
        )

        db.execSQL("DROP TABLE custom_lists")
        db.execSQL("ALTER TABLE custom_lists_new RENAME TO custom_lists")
    }
}

val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE tv_seasons ADD COLUMN lastEpWatched INTEGER
        """.trimIndent()
        )
    }
}

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE trending_data_new (
                mainUuid TEXT PRIMARY KEY NOT NULL,
                id INTEGER NOT NULL,
                name TEXT,
                title TEXT,
                backdropPath TEXT NOT NULL,
                posterPath TEXT NOT NULL,
                mediaType TEXT NOT NULL,
                releaseDate TEXT,
                firstAirDate TEXT,
                avgRating REAL,
                overview TEXT NOT NULL,
                genreIds TEXT,
                cachedAt INTEGER NOT NULL,
                trendingType TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO trending_data_new (mainUuid, id, name, title, backdropPath, posterPath, mediaType, releaseDate, firstAirDate, avgRating, overview, genreIds, cachedAt, trendingType)
            SELECT mainUuid, id, name, title, backdropPath, posterPath, mediaType, releaseDate, firstAirDate, avgRating, overview, genreIds, cachedAt, 'day'
            FROM trending_data
        """.trimIndent()
        )

        db.execSQL("DROP TABLE trending_data")
        db.execSQL("ALTER TABLE trending_data_new RENAME TO trending_data")
    }
}

/**
 * Añade la fecha de visionado a los episodios.
 *
 * Nulo para todo lo que ya estaba: de los episodios marcados antes de esto no se guardó cuándo, y
 * inventar una fecha —la de hoy, la de emisión— habría llenado el diario de días en los que no
 * viste nada. Sin fecha simplemente no salen; los que marques a partir de ahora sí.
 */
val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tv_episodes ADD COLUMN watchedDate INTEGER")
    }
}
