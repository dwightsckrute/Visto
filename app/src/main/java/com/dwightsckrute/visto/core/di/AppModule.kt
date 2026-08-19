package com.dwightsckrute.visto.core.di

import com.dwightsckrute.visto.data.local.dao.MovieBundleDao
import com.dwightsckrute.visto.data.local.dao.WatchlistDao
import android.content.Context
import com.dwightsckrute.visto.data.local.VistoDatabase
import com.dwightsckrute.visto.data.repository.MovieRepository
import com.dwightsckrute.visto.data.repository.WatchlistRepository
import com.dwightsckrute.visto.core.network.TmdbApi
import com.dwightsckrute.visto.data.local.dao.CustomListsDao
import com.dwightsckrute.visto.data.local.dao.SeasonDao
import com.dwightsckrute.visto.data.local.dao.TrendingDao
import com.dwightsckrute.visto.data.local.dao.TvBundleDao
import com.dwightsckrute.visto.data.local.dao.TvEpisodeDao
import com.dwightsckrute.visto.data.repository.CustomListsRepository
import com.dwightsckrute.visto.data.repository.PersonRepository
import com.dwightsckrute.visto.data.repository.SearchRepository
import com.dwightsckrute.visto.data.repository.TrendingRepository
import dagger.Module
import com.dwightsckrute.visto.core.network.OpenLibraryApi
import com.dwightsckrute.visto.data.repository.BookRepository
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.dwightsckrute.visto.data.repository.TvRepository

@Module
@InstallIn(SingletonComponent::class)
@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): VistoDatabase =
        VistoDatabase.getInstance(context)

    @Provides
    fun provideWatchlistDao(db: VistoDatabase) =
        db.watchlistDao()

    @Provides
    fun provideMovieBundleDao(db: VistoDatabase) =
        db.movieBundleDao()

    @Provides
    fun provideTvBundleDao(db: VistoDatabase) =
        db.tvBundleDao()

    @Provides
    fun provideSeasonDao(db: VistoDatabase) =
        db.seasonDao()

    @Provides
    fun provideTvEpisodeDao(db: VistoDatabase) =
        db.tvEpisodeDao()

    @Provides
    fun provideMovieListsDao(db: VistoDatabase) =
        db.movieListsDao()

    @Provides
    fun provideTrendingDao(db: VistoDatabase) =
        db.trendingDao()


    @Provides
    @Singleton
    fun provideTmdbApi(
        @ApplicationContext context: Context
    ): TmdbApi = TmdbApi.create(context.cacheDir)

    @Provides
    @Singleton
    fun provideMovieRepository(
        api: TmdbApi,
        movieBundleDao: MovieBundleDao
    ): MovieRepository =
        MovieRepository(api, movieBundleDao)

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        watchlistDao: WatchlistDao,
        seasonDao: SeasonDao,
        movieRepository: MovieRepository,
        tvRepository: TvRepository
    ): WatchlistRepository =
        WatchlistRepository(watchlistDao, seasonDao, movieRepository, tvRepository)

    @Provides
    @Singleton
    fun provideSearchRepository(
        api: TmdbApi,
        bookRepository: BookRepository,
    ): SearchRepository =
        SearchRepository(api, bookRepository)

    @Provides
    @Singleton
    fun providerTvRepository(
        api: TmdbApi,
        tvBundleDao: TvBundleDao,
        tvEpisodeDao: TvEpisodeDao,
        seasonDao: SeasonDao
    ): TvRepository = TvRepository(api, tvBundleDao, tvEpisodeDao, seasonDao)

    @Provides
    @Singleton
    fun provideCustomListsRepository(
        movieListsDao: CustomListsDao
    ): CustomListsRepository = CustomListsRepository(movieListsDao)

    @Provides
    @Singleton
    fun providePersonRepository(
        api: TmdbApi
    ): PersonRepository = PersonRepository(api)

    @Provides
    @Singleton
    fun provideTrendingRepository(
        api: TmdbApi,
        dao: TrendingDao
    ): TrendingRepository = TrendingRepository(api, dao)

    @Provides
    @Singleton
    fun provideOpenLibraryApi(
        @ApplicationContext context: Context,
    ): OpenLibraryApi = OpenLibraryApi.create(context.cacheDir)

    @Provides
    @Singleton
    fun provideBookRepository(
        api: OpenLibraryApi,
        dao: WatchlistDao,
    ): BookRepository = BookRepository(api, dao)
}