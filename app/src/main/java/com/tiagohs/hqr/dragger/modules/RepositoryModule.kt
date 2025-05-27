package com.tiagohs.hqr.dragger.modules

package com.tiagohs.hqr.dragger.modules

import com.tiagohs.hqr.database.*
import com.tiagohs.hqr.database.repository.*
import dagger.Module
import dagger.Provides
import io.realm.kotlin.RealmConfiguration // Import RealmConfiguration

@Module
class RepositoryModule {

    @Provides
    fun providerSourceRepository(realmConfiguration: RealmConfiguration): ISourceRepository {
        return SourceRepository(realmConfiguration)
    }

    @Provides
    fun providerChapterRepository(realmConfiguration: RealmConfiguration): IChapterRepository {
        return ChapterRepository(realmConfiguration)
    }

    @Provides
    fun providerComicRepository(
        sourceRepository: ISourceRepository,
        realmConfiguration: RealmConfiguration
    ): IComicsRepository {
        return ComicsRepository(sourceRepository, realmConfiguration)
    }

    @Provides
    fun providerHistoryRepository(realmConfiguration: RealmConfiguration): IHistoryRepository {
        return HistoryRepository(realmConfiguration)
    }

    @Provides
    fun providerDefaultModelsRepository(
        sourceRepository: ISourceRepository,
        realmConfiguration: RealmConfiguration
    ): IDefaultModelsRepository {
        return DefaultModelsRepository(sourceRepository, realmConfiguration)
    }

}