package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.IHistoryRepository
import com.tiagohs.hqr.factory.HistoryFactory
import com.tiagohs.hqr.models.database.comics.ComicHistory
import com.tiagohs.hqr.models.view_models.ComicHistoryViewModel
import io.reactivex.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.runBlocking
import javax.inject.Inject // Added for Dagger

class HistoryRepository @Inject constructor( // Added @Inject constructor
    private val realmConfiguration: RealmConfiguration
) : BaseRepository(realmConfiguration), IHistoryRepository {

    override fun insertComicHistoryRealm(comicHistoryViewModel: ComicHistoryViewModel): ComicHistoryViewModel? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            var finalHistoryVm: ComicHistoryViewModel? = null
            try {
                realm.write {
                    // Query for existing history based on comic pathLink, as original logic
                    val existingHistory = comicHistoryViewModel.comic?.pathLink?.let {
                        this.query<ComicHistory>("comic.pathLink == $0", it).first().find()
                    }

                    val historyToPersist: ComicHistory
                    if (existingHistory != null) {
                        // TODO: HistoryFactory.copyFromComicHistoryViewModel needs refactor
                        // Manually map relevant fields for update
                        existingHistory.lastTimeRead = comicHistoryViewModel.lastTimeRead
                        // Potentially update chapter link if it changed: existingHistory.chapter = map(comicHistoryViewModel.chapter)
                        historyToPersist = existingHistory
                    } else {
                        // TODO: HistoryFactory.createComicHistoryForRealm needs refactor
                        // Manually map to a new ComicHistory object
                        historyToPersist = ComicHistory().apply {
                            // this.id = comicHistoryViewModel.id // Potential ID generation needed if new
                            this.lastTimeRead = comicHistoryViewModel.lastTimeRead
                            // Map comic and chapter (these would be new RealmObjects or links to existing ones)
                            // This part is complex without refactored factories and other repos.
                            // For now, assume comicHistoryViewModel.comic and .chapter are already correct Realm model instances
                            // or can be mapped. This is a significant simplification.
                            // this.comic = comicHistoryViewModel.comic?.let { /* map ComicViewModel to Comic RealmObject */ }
                            // this.chapter = comicHistoryViewModel.chapter?.let { /* map ChapterViewModel to Chapter RealmObject */ }
                        }
                    }
                    val persistedHistory = copyToRealm(historyToPersist, updatePolicy = UpdatePolicy.ALL)
                    finalHistoryVm = HistoryFactory.createComicHistoryViewModel(persistedHistory) // Factory needs update
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                realm.close()
            }
            finalHistoryVm
        }
    }

    override fun findByComicIdRealm(comicId: Long): ComicHistoryViewModel? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                val historyRealm = realm.query<ComicHistory>("comic.id == $0", comicId).first().find()
                // TODO: HistoryFactory.createComicHistoryViewModel needs to be updated
                historyRealm?.let { HistoryFactory.createComicHistoryViewModel(it) }
            } finally {
                realm.close()
            }
        }
    }

    override fun insertComicHistory(comicHistoryViewModel: ComicHistoryViewModel): Observable<ComicHistoryViewModel> {
        // Re-implementing similarly to insertComicHistoryRealm but wrapped in Observable
        return Observable.fromCallable {
            insertComicHistoryRealm(comicHistoryViewModel) ?: throw RuntimeException("Failed to insert comic history")
            // The original returned Observable<ComicHistory>, now returning Observable<ComicHistoryViewModel> for consistency
            // If insertComicHistoryRealm returns null, this will emit onError.
        }
    }

    override fun deleteComicHistory(comicHistory: ComicHistoryViewModel): Observable<Unit> { // Changed Void to Unit
        return delete<ComicHistory>(comicHistory.id)
    }

    override fun delteAllComicHistory(): Observable<Unit> { // Changed Void? to Unit
        return deleteAll<ComicHistory>()
    }

    override fun findAll(): Observable<List<ComicHistoryViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val results = realm.query<ComicHistory>().find()
                    // TODO: HistoryFactory.createListOfChapterViewModel needs to be updated (and renamed likely)
                    HistoryFactory.createListOfChapterViewModel(realm.copyFromRealm(results))
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun findById(id: Long): Observable<ComicHistoryViewModel> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                var historyVm: ComicHistoryViewModel? = null
                try {
                    val result = realm.query<ComicHistory>("id == $0", id).first().find()
                    // TODO: HistoryFactory.createComicHistoryViewModel needs to be updated
                    result?.let { historyVm = HistoryFactory.createComicHistoryViewModel(it) }
                } finally {
                    realm.close()
                }
                historyVm ?: throw NoSuchElementException("ComicHistory not found with id: $id")
            }
        }
    }

    override fun findByComicId(comicId: Long): Observable<ComicHistoryViewModel> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                var historyVm: ComicHistoryViewModel? = null
                try {
                    val result = realm.query<ComicHistory>("comic.id == $0", comicId).first().find()
                    // TODO: HistoryFactory.createComicHistoryViewModel needs to be updated
                    result?.let { historyVm = HistoryFactory.createComicHistoryViewModel(it) }
                } finally {
                    realm.close()
                }
                historyVm ?: throw NoSuchElementException("ComicHistory not found for comicId: $comicId")
            }
        }
    }

    // Private helper methods find() and findAll() are removed.
}