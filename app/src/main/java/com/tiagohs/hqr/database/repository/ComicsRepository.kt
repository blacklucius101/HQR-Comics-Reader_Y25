package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.IComicsRepository
import com.tiagohs.hqr.database.ISourceRepository
import com.tiagohs.hqr.factory.ComicsFactory
import com.tiagohs.hqr.factory.ComicsFactory.createListOfComicModelFormRealm
import com.tiagohs.hqr.factory.ComicsFactory.createListOfComicViewModel
import com.tiagohs.hqr.models.base.IComic
import com.tiagohs.hqr.models.database.SourceDB
import com.tiagohs.hqr.models.database.comics.Chapter
import com.tiagohs.hqr.database.IComicsRepository
import com.tiagohs.hqr.database.ISourceRepository
import com.tiagohs.hqr.factory.ComicsFactory
// import com.tiagohs.hqr.factory.ComicsFactory.createListOfComicModelFormRealm // Will address later
// import com.tiagohs.hqr.factory.ComicsFactory.createListOfComicViewModel // Will address later
import com.tiagohs.hqr.models.base.IComic
import com.tiagohs.hqr.models.database.SourceDB
import com.tiagohs.hqr.models.database.comics.Chapter
import com.tiagohs.hqr.models.database.comics.Comic
import com.tiagohs.hqr.models.view_models.ComicViewModel
import io.reactivex.Observable
// import io.realm.Case // Replaced by query language options if needed
// import io.realm.Realm // Old Realm
// import io.realm.RealmQuery // Old Realm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import javax.inject.Inject // Added for Dagger

class ComicsRepository @Inject constructor( // Added @Inject constructor
    private val sourceRepository: ISourceRepository,
    private val realmConfiguration: RealmConfiguration
) : BaseRepository(realmConfiguration), IComicsRepository {

import io.realm.kotlin.UpdatePolicy // Added for copyToRealm

// ... (imports and constructor)

    override fun insertRealm(comicVm: ComicViewModel, sourceId: Long, skipFavorite: Boolean?): ComicViewModel? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            var finalComicVm: ComicViewModel? = null
            try {
                val sourceDb: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)

                if (sourceDb == null) {
                    // Log.e("ComicsRepository", "SourceDB not found for ID: $sourceId. Cannot insert/update comic.")
                    return@runBlocking null
                }

                val managedComic = realm.write {
                    val existingComicDb = this.query<Comic>("pathLink == $0 AND source.id == $1", comicVm.pathLink, sourceId).first().find()
                    
                    if (existingComicDb != null) {
                        // Use factory to update the existing managed RealmObject directly
                        ComicsFactory.updateComicFromViewModel(existingComicDb, comicVm, sourceDb, skipFavorite)
                        existingComicDb // return the managed, updated object
                    } else {
                        // Use factory to create a new detached Comic object
                        val newDetachedComic = ComicsFactory.createComicFromViewModel(comicVm, sourceDb)
                        // Persist the new detached object, it becomes managed
                        copyToRealm(newDetachedComic, updatePolicy = UpdatePolicy.ALL)
                    }
                }
                // Map the persisted (managed) Realm object back to ViewModel
                // Ensure createComicViewModel can handle managed objects; copyFromRealm might be needed if it expects detached.
                // Given our factory refactor, it should work with detached copies.
                // So, we copy from realm THEN map.
                finalComicVm = ComicsFactory.createComicViewModel(realm.copyFromRealm(managedComic))
            } catch (ex: Exception) {
                ex.printStackTrace() // Proper error logging
            } finally {
                realm.close()
            }
            finalComicVm
        }
        /*
        val realm = Realm.getDefaultInstance()
        val localComic = ComicViewModel()

        try {
            val source: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)
            var result = realm.where(Comic::class.java)
                                      .equalTo("source.id", sourceId)
                                      .equalTo("pathLink", comic.pathLink)
                                      .findFirst()

            realm.executeTransaction { r ->
                if (result != null) {
                    result = ComicsFactory.copyFromComicViewModel(result!!, comic, source!!, r, skipFavorite)
                } else {
                    result = ComicsFactory.createComicModelForRealm(comic, source, r)
                }

                r.insertOrUpdate(result)
            }

            localComic.copyFrom(result!!)

            finishTransaction(realm)
        } catch (ex: Exception) {
            if (!realm.isClosed)
                realm.close()

            throw ex
        }

        return localComic
        */
    }

    override fun insertRealm(comicsVmList: List<ComicViewModel>, sourceId: Long): List<ComicViewModel>? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            val finalComicVmList = mutableListOf<ComicViewModel>()
            try {
                val sourceDb: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)
                if (sourceDb == null) {
                    // Log.e("ComicsRepository", "SourceDB not found for ID: $sourceId. Cannot insert/update comics list.")
                    return@runBlocking null
                }

                val persistedComics = realm.write {
                    val comicsToPersist = mutableListOf<Comic>()
                    comicsVmList.forEach { comicVm ->
                        val existingComicDb = this.query<Comic>("pathLink == $0 AND source.id == $1", comicVm.pathLink, sourceId).first().find()
                        if (existingComicDb != null) {
                            ComicsFactory.updateComicFromViewModel(existingComicDb, comicVm, sourceDb, false) // skipFavorite = false
                            comicsToPersist.add(existingComicDb)
                        } else {
                            val newDetachedComic = ComicsFactory.createComicFromViewModel(comicVm, sourceDb)
                            comicsToPersist.add(copyToRealm(newDetachedComic, updatePolicy = UpdatePolicy.ALL))
                        }
                    }
                    comicsToPersist // Return list of managed objects
                }
                // Map the persisted (managed) Realm objects back to ViewModels
                finalComicVmList.addAll(ComicsFactory.createListOfComicViewModel(realm.copyFromRealm(persistedComics)))
            } catch (ex: Exception) {
                ex.printStackTrace() // Proper error logging
            } finally {
                realm.close()
            }
            if (finalComicVmList.isEmpty()) null else finalComicVmList
        }
        /*
        val realm = Realm.getDefaultInstance()
        var comicsLocal: List<Comic>?
        var comicsLocalFinal: List<ComicViewModel>? = null

        try {
            val source: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)
            realm.executeTransaction { r ->
                comicsLocal = createListOfComicModelFormRealm(comics, source!!, r)

                r.insertOrUpdate(comicsLocal)

                comicsLocalFinal = createListOfComicViewModel(comicsLocal!!)
            }

            finishTransaction(realm)
        } catch (ex: Exception) {
            if (!realm.isClosed)
                realm.close()

            throw ex
        }

        return comicsLocalFinal
        */
    }

    override fun findByPathUrlRealm(pathLink: String, sourceId: Long): ComicViewModel? {
        return runBlocking { // Bridge for now
            val realm = Realm.open(realmConfiguration)
            try {
                val comicRealm = realm.query<Comic>("pathLink == $0 AND source.id == $1", pathLink, sourceId).first().find()
                comicRealm?.let { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
            } finally {
                realm.close()
            }
        }
    }

    override fun findByIdRealm(comicId: Long): ComicViewModel? {
        return runBlocking { // Bridge for now
            val realm = Realm.open(realmConfiguration)
            try {
                val comicRealm = realm.query<Comic>("id == $0", comicId).first().find()
                comicRealm?.let { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
            } finally {
                realm.close()
            }
        }
    }

import io.realm.kotlin.query.Sort // For sorting, if needed later

// ... (constructor and previously refactored findByIdRealm, findByPathUrlRealm)

    override fun isFavorite(pathLink: String): Boolean {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                realm.query<Comic>("pathLink == $0 AND favorite == true", pathLink).first().find() != null
            } finally {
                realm.close()
            }
        }
    }

    override fun insertOrUpdateComic(comic: ComicViewModel, sourceId: Long, skipFavorite: Boolean?): Observable<ComicViewModel> {
        // TODO: Refactor. This will involve sourceRepository returning Observable<SourceDB_ViewModel_or_ID>,
        // then flatMap to do the realm write operation.
        // The insertRealm method itself needs full refactoring.
        return sourceRepository.getSourceById(sourceId)
                .map { source -> insertRealm(comic, sourceId, skipFavorite) }
    }

    override fun insertOrUpdateComics(comics: List<ComicViewModel>, sourceId: Long): Observable<List<ComicViewModel>> {
        return sourceRepository.getSourceById(sourceId)
                .map { source -> insertRealm(comics, sourceId) }
    }

    override fun searchComic(query: String, sourceId: Long): Observable<List<ComicViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    // Case insensitivity is usually handled by Realm's query language or by transforming the query string.
                    // For Realm Kotlin, it's often `CONTAINS[c]` for case-insensitive.
                    val comicsRealm = realm.query<Comic>("source.id == $0 AND name CONTAINS[c] $1", sourceId, query).find()
                    comicsRealm.map { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun addOrRemoveFromFavorite(comicVmInput: ComicViewModel, sourceId: Long): Observable<ComicViewModel> {
        return sourceRepository.getSourceById(sourceId) // Assuming this returns Observable<SourceDB?>
            .flatMap { sourceDb ->
                if (sourceDb == null) return@flatMap Observable.error<ComicViewModel>(Exception("Source not found for addOrRemoveFromFavorite"))
                Observable.fromCallable {
                    runBlocking {
                        val realm = Realm.open(realmConfiguration)
                        var resultingVm = comicVmInput // Default to input if no change or error
                        try {
                            val persistedComic = realm.write {
                                val comicRealmDb = this.query<Comic>("pathLink == $0 AND source.id == $1", comicVmInput.pathLink, sourceDb.id).first().find()
                                if (comicRealmDb != null) {
                                    comicRealmDb.favorite = !comicRealmDb.favorite // Toggle favorite
                                    copyToRealm(comicRealmDb, UpdatePolicy.ALL) // Persist change
                                } else {
                                    // If comic doesn't exist, create it and set favorite status from comicVmInput (which should be toggled before calling)
                                    // This assumes comicVmInput.favorite already reflects the desired new state.
                                    val newDetachedComic = ComicsFactory.createComicFromViewModel(comicVmInput, sourceDb)
                                    copyToRealm(newDetachedComic, UpdatePolicy.ALL)
                                }
                            }
                            // Re-fetch or use the returned managed object to create the ComicViewModel
                            val finalComicState = realm.query<Comic>("pathLink == $0 AND source.id == $1", comicVmInput.pathLink, sourceDb.id).first().find()
                            finalComicState?.let { resultingVm = ComicsFactory.createComicViewModel(realm.copyFromRealm(it)) }
                        } finally {
                            realm.close()
                        }
                        resultingVm
                    }
                }
            }
    }

    override fun setAsDownloaded(comicVm: ComicViewModel, sourceId: Long): Observable<ComicViewModel> {
        return sourceRepository.getSourceById(sourceId) 
            .flatMap { sourceDb ->
                if (sourceDb == null) return@flatMap Observable.error<ComicViewModel>(Exception("Source not found for setAsDownloaded"))
                Observable.fromCallable {
                    runBlocking {
                        val realm = Realm.open(realmConfiguration)
                        var updatedComicVm = comicVm
                        try {
                            realm.write {
                                val comicRealm = this.query<Comic>("pathLink == $0 AND source.id == $1", comicVm.pathLink, sourceId).first().find()
                                if (comicRealm != null && !comicRealm.downloaded) {
                                    comicRealm.downloaded = true
                                    val managedComic = copyToRealm(comicRealm, UpdatePolicy.ALL)
                                    updatedComicVm = ComicsFactory.createComicViewModel(managedComic)
                                }
                            }
                        } finally {
                            realm.close()
                        }
                        updatedComicVm
                    }
                }
            }
    }

    override fun setAsNotDownloaded(comicVm: ComicViewModel, sourceId: Long): Observable<ComicViewModel>  {
        return sourceRepository.getSourceById(sourceId) 
            .flatMap { sourceDb ->
                if (sourceDb == null) return@flatMap Observable.error<ComicViewModel>(Exception("Source not found for setAsNotDownloaded"))
                Observable.fromCallable {
                    runBlocking {
                        val realm = Realm.open(realmConfiguration)
                        var updatedComicVm = comicVm
                        try {
                            realm.write {
                                val comicRealm = this.query<Comic>("pathLink == $0 AND source.id == $1", comicVm.pathLink, sourceId).first().find()
                                if (comicRealm != null && comicRealm.downloaded) {
                                    comicRealm.downloaded = false
                                    val managedComic = copyToRealm(comicRealm, UpdatePolicy.ALL)
                                    updatedComicVm = ComicsFactory.createComicViewModel(managedComic)
                                }
                            }
                        } finally {
                            realm.close()
                        }
                        updatedComicVm
                    }
                }
            }
    }

    override fun deleteComic(comic: ComicViewModel): Observable<Unit> { 
        return delete<Comic>(comic.id)
    }

    override fun deleteAllComics(): Observable<Unit> { // Return type changed, and deleting Comics not Chapters
        return deleteAll<Comic>() // Changed from deleteAll<Chapter>() to deleteAll<Comic>()
    }

    override fun findAll(sourceId: Long): Observable<List<ComicViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val comicsRealm = realm.query<Comic>("source.id == $0", sourceId).find()
                    comicsRealm.map { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun findById(comicId: Long): Observable<ComicViewModel?> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val comicRealm = realm.query<Comic>("id == $0", comicId).first().find()
                    comicRealm?.let { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun findByPathUrl(pathLink: String, sourceId: Long): Observable<ComicViewModel?> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val comicRealm = realm.query<Comic>("pathLink == $0 AND source.id == $1", pathLink, sourceId).first().find()
                    comicRealm?.let { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getPopularComics(sourceId: Long): Observable<List<ComicViewModel>> {
        return getAllByTag(sourceId, IComic.POPULARS)
    }

    override fun getRecentsComics(sourceId: Long): Observable<List<ComicViewModel>> {
        return getAllByTag(sourceId, IComic.RECENTS)
    }

    private fun getAllByTag(sourceId: Long, tag: String): Observable<List<ComicViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    // Filtering by tag in a RealmList<String> requires a specific query.
                    // Example: "tags CONTAINS $0"
                    // The .filter in Kotlin is done after fetching all comics for the source, which is inefficient.
                    // This query should ideally be done at the Realm level if possible or the logic re-evaluated.
                    // For now, mimicking the existing logic:
                    val comicsRealm = realm.query<Comic>("source.id == $0 AND '$tag' IN tags", sourceId).find()
                    comicsRealm.map { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getFavoritesComics(): Observable<List<ComicViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val comicsRealm = realm.query<Comic>("favorite == true").find()
                    comicsRealm.map { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getDownloadedComics(): Observable<List<ComicViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val comicsRealm = realm.query<Comic>("downloaded == true").find()
                    comicsRealm.map { ComicsFactory.createComicViewModel(it) } // ComicsFactory will need update
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getTotalChapters(comic: ComicViewModel): Observable<Int> {
        // This relies on findById, which itself needs to be fully refactored from Observable.create
        // For now, assuming findById will be refactored to return a single ComicViewModel or null via Observable.fromCallable
        return findById(comic.id)
                .map { c -> c.chapters?.size ?: 0 }
    }

    override fun checkIfIsSaved(comics: List<ComicViewModel>): Observable<List<ComicViewModel>> {
        // TODO: Refactor this method for the new Realm SDK.
        // This involves querying for multiple comics by their IDs.
        // Example: val ids = comics.map { it.id }; realm.query<Comic>("id IN $0", ids).find()
        return Observable.empty() // Placeholder
        /*
        return Observable.create<List<ComicViewModel>> { emitter ->
            val realm = Realm.getDefaultInstance()

            try {
                val itemsId = comics.map { it.id }
                var results: ArrayList<Comic>? = null

                itemsId.forEach { id ->
                    val result = realm
                            .where(Comic::class.java)
                            .equalTo("id", id)
                            .findFirst()

                    if (result != null) {
                        if (results == null)
                            results = ArrayList<Comic>()

                        results?.add(result)
                    }
                }

                if (results != null) {
                    emitter.onNext(ComicsFactory.createListOfComicViewModel(results!!.toList()))
                }

                finishTransaction(realm)
                emitter.onComplete()
            } catch (ex: Exception) {
                if (!realm.isClosed)
                    realm.close()

                emitter.onError(ex)
            }
        }
        */
    }

    // Removed old private find and findAllComics methods as their logic will be inlined or handled differently.

}