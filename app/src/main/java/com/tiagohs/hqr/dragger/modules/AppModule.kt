package com.tiagohs.hqr.dragger.modules

import android.app.Application
import android.content.Context
import com.tiagohs.hqr.models.database.*
import com.tiagohs.hqr.models.database.comics.*
import dagger.Module
import dagger.Provides
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
class AppModule {

    lateinit var mApplication: Application

    constructor(application: Application) {
        this.mApplication = application
    }

    @Provides
    fun providesApplication(): Application {
        return mApplication
    }

    @Provides
    fun providerApplicationContext(): Context {
        return mApplication.applicationContext
    }

    @Provides
    @Singleton
    fun provideRealmConfiguration(): RealmConfiguration {
        return RealmConfiguration.create(schema = setOf(
            CatalogueSource::class, DefaultModel::class, SourceDB::class,
            Chapter::class, Comic::class, ComicHistory::class, Page::class
            // Add any other RealmObject classes from your project
        ))
    }

}