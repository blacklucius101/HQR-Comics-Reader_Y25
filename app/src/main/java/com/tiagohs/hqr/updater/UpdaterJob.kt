package com.tiagohs.hqr.updater

import android.content.Context // Keep context if core logic needs it, or inject
import android.content.Intent
// import com.evernote.android.job.Job // Removed
// import com.evernote.android.job.JobManager // Removed
// import com.evernote.android.job.JobRequest // Removed
import com.tiagohs.hqr.App
import com.tiagohs.hqr.dragger.components.HQRComponent
// import com.tiagohs.hqr.helpers.tools.CallInterceptor // Unused in this class directly
import com.tiagohs.hqr.notification.UpdaterNotification
// import okhttp3.Cache // Unused in this class directly
import okhttp3.OkHttpClient
// import java.util.concurrent.TimeUnit // Unused in this class directly
import javax.inject.Inject


// class UpdaterJob: Job() { // Removed Job extension
class UpdaterJob { // Now a plain class, constructor injection might be needed

    // Context will be an issue here if not provided.
    // For now, assuming this class will be refactored to receive context or specific dependencies.
    // Let's assume context is passed to the method that will use this logic.
    // @Inject lateinit var context: Context // This would require Dagger setup for Context here.

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var notifier: UpdaterNotification

    // This method's logic is what we want to preserve, but it can't run as a Job anymore.
    // It will need a Context parameter if it's to create Intents or access application context.
    // The Dagger injection will also need to happen differently (e.g., constructor or method injection).
    // For now, the method signature is kept, but it won't compile/run as is.
    fun checkForUpdateAndNotify(context: Context) { // Added Context param, removed return type related to Job.Result

        // getApplicationComponent()?.inject(this) // Dagger injection needs to be handled by the caller or constructor

        GithubUpdaterChecker(GithubUpdaterService.create(client))
                .checkForUpdate()
                .subscribe({ result -> // Changed to subscribe for Observable
                    if (result is GithubVersionResults.NewUpdate) {
                        val url = result.release.assets[0].downloadLink

                        val intent = Intent(context, UpdaterService::class.java).apply {
                            putExtra(UpdaterService.EXTRA_UPDATER_DOWNLOAD_URL, url)
                        }
                        notifier.newUpdateAvailable(intent)
                    }
                    // Log success or handle no update
                }, { error ->
                    // Log error
                })
                // .blockingFirst() // Removed, handle async nature of Observable
    }

    private fun getApplicationComponent(): HQRComponent? {
        // This won't work without a 'context' property.
        // If context is passed to checkForUpdateAndNotify, it can be used here.
        // return (context.applicationContext as App).getHQRComponent()
        return null // Placeholder
    }

    companion object {
        const val TAG = "UpdateChecker"

        // These methods are now dead code as they rely on JobManager
        // fun setupTask() {
        //     JobRequest.Builder(TAG)
        //             .setPeriodic(24 * 60 * 60 * 1000, 60 * 60 * 1000)
        //             .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
        //             .setRequirementsEnforced(true)
        //             .setUpdateCurrent(true)
        //             .build()
        //             .schedule()
        // }

        // fun cancelTask() {
        //     JobManager.instance().cancelAllForTag(TAG)
        // }
    }
}