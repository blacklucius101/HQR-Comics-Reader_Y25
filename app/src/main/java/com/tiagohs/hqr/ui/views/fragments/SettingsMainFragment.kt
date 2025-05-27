package com.tiagohs.hqr.ui.views.fragments

import android.Manifest
import android.app.Activity // Needed for onActivityResult
import android.content.Intent // Needed for onActivityResult
import android.net.Uri
import android.os.Bundle
import android.os.Environment // For initial path
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import androidx.browser.customtabs.CustomTabsIntent
import android.text.format.Formatter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPositive
import com.afollestad.materialdialogs.files.folderChooser // New import for folder chooser
import com.tiagohs.hqr.App
import com.tiagohs.hqr.BuildConfig
import com.tiagohs.hqr.R
import com.tiagohs.hqr.download.DownloadProvider
import com.tiagohs.hqr.dragger.components.HQRComponent
import com.tiagohs.hqr.helpers.extensions.getResourceColor
import com.tiagohs.hqr.helpers.extensions.hasPermission
import com.tiagohs.hqr.helpers.extensions.toast
import com.tiagohs.hqr.helpers.tools.PreferenceHelper
import com.tiagohs.hqr.helpers.utils.DiskUtils
import com.tiagohs.hqr.helpers.utils.LocaleUtils
import com.tiagohs.hqr.updater.GithubUpdaterChecker
import com.tiagohs.hqr.updater.GithubVersionResults
import com.tiagohs.hqr.updater.UpdaterJob
import com.tiagohs.hqr.updater.UpdaterService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SettingsMainFragment: PreferenceFragment() {

    companion object {
        const val DOWNLOAD_DIR_PRE_L = 103
        const val DOWNLOAD_DIR_L = 104

        fun newFragment(): SettingsMainFragment {
            return SettingsMainFragment()
        }
    }

    @Inject
    lateinit var preferences: PreferenceHelper

    @Inject
    lateinit var downloadProvider: DownloadProvider

    @Inject
    lateinit var localeUtils: LocaleUtils

    @Inject
    lateinit var updaterChecker: GithubUpdaterChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_main)

        getApplicationComponent()?.inject(this)

        val hasPremissionToWrite= activity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasPremissionToWrite) {
            configureStorageUsed(findPreference(getString(R.string.key_download_storage_used)) as Preference)
            configureCacheStorageUsed(findPreference(getString(R.string.key_download_cache_used)) as Preference)
            configureDownloadDirectory(findPreference(getString(R.string.key_download_directory)) as Preference) // Added call
        } else {
            preferenceScreen.removePreference(findPreference(getString(R.string.key_category_downloads)))
        }

        configureGithubUrl(findPreference(getString(R.string.key_github)) as Preference)
        configureUpdateVersion(findPreference(getString(R.string.key_update_version)) as Preference)
        configureCheckForUpdatesAutomatically(findPreference(getString(R.string.auto_updates_key)) as SwitchPreference)
    }

    private fun configureDownloadDirectory(preference: Preference) {
        preference.summary = preferences.getDownloadsDirectory() // Display current path

        preference.setOnPreferenceClickListener {
            val currentPath = preferences.getDownloadsDirectory()
            val initialFolder = if (currentPath.isNotEmpty()) java.io.File(currentPath) else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            MaterialDialog(activity).show {
                folderChooser(
                    initialDirectory = initialFolder,
                    allowFolderCreation = true,
                    context = requireContext() // Ensure context is passed if extension function needs it
                ) { _, folder ->
                    val selectedPath = folder.absolutePath
                    preferences.setDownloadsDirectory(selectedPath)
                    preference.summary = selectedPath
                }
                title(R.string.title_download_directory)
                positiveButton(android.R.string.ok)
                negativeButton(android.R.string.cancel)
            }
            true
        }
    }

    private fun configureCheckForUpdatesAutomatically(switchPreference: SwitchPreference) {
        switchPreference.setOnPreferenceChangeListener { preference, newValue ->

            if (switchPreference.isChecked) {
                UpdaterJob.cancelTask()

                switchPreference.isChecked = false
            } else {
                UpdaterJob.setupTask()

                switchPreference.isChecked = true
            }

            false
        }
    }

    private fun configureUpdateVersion(preference: Preference) {
        preference.setSummary(BuildConfig.VERSION_NAME)

        preference.setOnPreferenceClickListener { p ->
            activity.toast(getString(R.string.checking_updates))

            updaterChecker.checkForUpdate()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->

                        when (result) {
                            is GithubVersionResults.NewUpdate -> {
                                val body = result.release.changeLog
                                val url = result.release.assets[0].downloadLink

                                MaterialDialog(activity).show {
                                    title(R.string.check_new_update_avalible)
                                    message(text = body)
                                    positiveButton(R.string.download) {
                                        UpdaterService.downloadUpdate(activity, url)
                                    }
                                    negativeButton(R.string.ignore)
                                }

                            }
                            is GithubVersionResults.NoNewUpdate -> {
                                activity.toast(getString(R.string.no_updates))
                            }
                        }

                    }, { error -> activity.toast(R.string.unknown_error) })

            true
        }
    }

    private fun configureStorageUsed(preference: Preference) {
        preference.setSummary(getString(R.string.storage_used, downloadProvider.getDownloadDirectorySize()))
        preference.setOnPreferenceClickListener { p ->

            MaterialDialog(activity).show {
                message(R.string.clear_storage_content)
                positiveButton(android.R.string.yes) {
                    clearStorageFolder(preference)
                }
                negativeButton(android.R.string.no)
            }

            true
        }
    }

    private fun clearStorageFolder(preference: Preference) {

        try {
            downloadProvider.deleteAll()
            preference.setSummary(getString(R.string.storage_used, downloadProvider.getDownloadDirectorySize()))

            activity.toast(R.string.sucess_storage_clear)
        } catch (ex: Exception) {
            activity.toast(R.string.unknown_error)
        }

    }

    private fun configureGithubUrl(preference: Preference) {

        preference.setOnPreferenceClickListener { p ->
            val context = view?.context

            if (context != null) {
                try {
                    val url = Uri.parse(getString(R.string.summary_github))
                    val intent = CustomTabsIntent.Builder()
                            .setToolbarColor(context.getResourceColor(R.color.colorPrimary))
                            .setShowTitle(true)
                            .build()
                    intent.launchUrl(activity, url)
                } catch (e: Exception) {
                    context.toast(e.message)
                }
            }

            true
        }

    }

    private fun configureCacheStorageUsed(preference: Preference) {
        preference.setSummary(getString(R.string.storage_used, getPicassoCacheDirSize()))
        preference.setOnPreferenceClickListener { p ->

            MaterialDialog(activity).show {
                message(R.string.clear_cache_storage_content)
                positiveButton(android.R.string.yes) {
                    clearCacheFolder(preference)
                }
                negativeButton(android.R.string.no)
            }

            true
        }
    }

    private fun clearCacheFolder(preference: Preference) {

        try {
            DiskUtils.getPicassoCacheDir(activity.applicationContext)?.deleteRecursively()
            preference.setSummary(getString(R.string.storage_used, getPicassoCacheDirSize()))

            activity.toast(R.string.sucess_cache_clear)
        } catch (ex: Exception) {
            activity.toast(R.string.unknown_error)
        }
    }

    private fun getPicassoCacheDirSize(): String {
        val picassoDir = DiskUtils.getPicassoCacheDir(activity.applicationContext)
        val size = if (picassoDir != null)
            DiskUtils.getDirectorySize(picassoDir)
        else
            0L

        return Formatter.formatFileSize(activity.applicationContext, size)
    }

    private fun getApplicationComponent(): HQRComponent? {
        return (activity?.application as App).getHQRComponent()
    }

    // Remove onActivityResult if it was only for the file picker
    // For now, assuming it might be used by other things, so just commenting out the relevant part
    // If it's confirmed to be unused elsewhere, the entire method can be deleted.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // if (requestCode == DOWNLOAD_DIR_L || requestCode == DOWNLOAD_DIR_PRE_L) {
        //     if (resultCode == Activity.RESULT_OK && data != null) {
        //         val uri = data.data
        //         if (uri != null) {
        //             val path = com.nononsenseapps.filepicker.Utils.getSafFullPath(activity, uri)
        //             if (path != null) {
        //                 preferences.setDownloadsDirectory(path)
        //                 findPreference(getString(R.string.key_download_directory))?.summary = path
        //             } else {
        //                 activity.toast(R.string.unknown_error)
        //             }
        //         }
        //     }
        // }
    }
}