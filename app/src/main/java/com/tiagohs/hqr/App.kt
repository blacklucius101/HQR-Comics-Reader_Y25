package com.tiagohs.hqr

package com.tiagohs.hqr

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex // Updated import
// import com.evernote.android.job.JobManager // Removed Evernote Job
// import com.facebook.stetho.Stetho // Removed Stetho
import com.squareup.picasso.Picasso
// import com.tiagohs.hqr.database.HQRInitialData // Will be handled with Realm config if needed
import com.tiagohs.hqr.dragger.components.DaggerHQRComponent
import com.tiagohs.hqr.dragger.components.HQRComponent
import com.tiagohs.hqr.dragger.modules.AppModule
import com.tiagohs.hqr.models.database.*
import com.tiagohs.hqr.models.database.comics.*
import com.tiagohs.hqr.notification.Notifications
import com.tiagohs.hqr.updater.UpdaterJob // Keep if UpdaterJob is used by something else, remove if only for android-job
// import com.uphyca.stetho_realm.RealmInspectorModulesProvider // Old Realm - Removed
// import io.realm.Realm // Old Realm - Removed
// import io.realm.RealmConfiguration // Old Realm - Removed
import io.realm.kotlin.RealmConfiguration // New Realm
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber // Added Timber import
import com.tiagohs.hqr.BuildConfig // Added BuildConfig for Timber



class App : Application() {

    private var instance: App? = null

    private var mHQRComponent: HQRComponent? = null

    override fun onCreate() {
        super.onCreate()

        onConfigureDagger()
        onConfigureRealm() // RealmConfiguration now provided by Dagger
        setupNotificationChannels()
        // setupJobManager() // Removed Evernote Job setup
        onConfigurePicasso()
        setupTimber() // Added Timber setup call

        instance = this

        MultiDex.install(this);

        RxJavaPlugins.setErrorHandler { throwable -> }
    }

    private fun onConfigureDagger() {
        mHQRComponent = DaggerHQRComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

    private fun onConfigureRealm() {
        // Realm.init(this) // Old Realm init - removed

        // Old RealmConfiguration - commented out
        /*
        Realm.setDefaultConfiguration(
                with(io.realm.RealmConfiguration.Builder()) { // Disambiguate for clarity
                    name("hqr_db.realm")
                    schemaVersion(1)

                    deleteRealmIfMigrationNeeded()
                    initialData( { realm ->
                        HQRInitialData.initialData(realm).forEach { catalogueSource: CatalogueSource ->
                            realm.createObject(CatalogueSource::class.java, catalogueSource.id).apply {
                                this.language = catalogueSource.language
                                // this.sourceDBS = catalogueSource.sourceDBS // Needs careful migration
                            }
                        }

                        realm.close()
                    })
                build()
        })
        */

        // New Realm Configuration - This is now provided by Dagger in AppModule.kt
        // val realmConfiguration = RealmConfiguration.create(schema = setOf(
        //     CatalogueSource::class, DefaultModel::class, SourceDB::class,
        //     Chapter::class, Comic::class, ComicHistory::class, Page::class
        // ))

        // The actual Realm instance should be opened when needed, typically injected.
        // For now, we just define the configuration.
        // Example: val realm = Realm.open(realmConfiguration)
        // We will not set a default instance as the new SDK encourages instance management.

        // Stetho Realm Inspector is for the old Realm, comment out or remove
        // Stetho related code already commented/removed.
    }

    // protected fun setupJobManager() { // Removed Evernote Job setup
    //     JobManager.create(this).addJobCreator { tag ->
    //         when (tag) {
    //             UpdaterJob.TAG -> UpdaterJob()
    //             else -> null
    //         }
    //     }
    // }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Optionally, plant a release tree for crash reporting if you add one later
            // Timber.plant(CrashReportingTree())
        }
    }

    private fun onConfigurePicasso() {
        val build = Picasso.Builder(this)
                                        //.downloader(OkHttp3Downloader(applicationContext, Integer.MAX_VALUE.toLong()))
                                        .build()

        build.setIndicatorsEnabled(true)
        build.isLoggingEnabled = true

        Picasso.setSingletonInstance(build)
    }

    fun getHQRComponent(): HQRComponent? {
        return mHQRComponent
    }

    private fun setupNotificationChannels() {
        Notifications.createChannels(this)
    }

    fun getInstance(): App? {
        return instance
    }
}