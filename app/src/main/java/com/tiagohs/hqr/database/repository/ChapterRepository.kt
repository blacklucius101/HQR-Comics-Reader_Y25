package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.IChapterRepository
import com.tiagohs.hqr.factory.ChapterFactory
import com.tiagohs.hqr.models.database.comics.Chapter
import com.tiagohs.hqr.models.view_models.ChapterViewModel
import io.reactivex.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.runBlocking
import javax.inject.Inject // Added for Dagger

class ChapterRepository @Inject constructor( // Added @Inject constructor
    private val realmConfiguration: RealmConfiguration
) : BaseRepository(realmConfiguration), IChapterRepository {

    override fun getChapterRealm(pathLink: String, comicId: Long): ChapterViewModel? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                val chapterRealm = realm.query<Chapter>("chapterPath == $0 AND comic.id == $1", pathLink, comicId).first().find()
                // TODO: ChapterViewModel().create(result) needs to be replaced with proper mapping.
                // For now, assuming ChapterFactory or similar direct mapping.
                chapterRealm?.let { ChapterViewModel().create(it) } // Placeholder for proper mapping
            } finally {
                realm.close()
            }
        }
    }

    override fun getAllChapters(comicId: Long): Observable<List<ChapterViewModel>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val results = realm.query<Chapter>("comic.id == $0", comicId).find()
                    // TODO: ChapterFactory.createListOfChapterViewModel needs to be updated for new RealmObject List
                    ChapterFactory.createListOfChapterViewModel(realm.copyFromRealm(results))
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun setOrUnsetAsDownloaded(chapterViewModel: ChapterViewModel, comicId: Long): ChapterViewModel {
        runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                realm.writeBlocking {
                    val chapter = this.query<Chapter>("chapterPath == $0 AND comic.id == $1", chapterViewModel.chapterPath, comicId).first().find()
                    chapter?.let {
                        it.downloaded = !it.downloaded
                        copyToRealm(it, updatePolicy = UpdatePolicy.ALL) // Persist changes
                        chapterViewModel.downloaded = it.downloaded // Update ViewModel
                    }
                }
            } finally {
                realm.close()
            }
        }
        return chapterViewModel
    }

    override fun removeAllDownloads(comicId: Long) {
        runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                realm.writeBlocking {
                    val chapters = this.query<Chapter>("comic.id == $0 AND downloaded == true", comicId).find()
                    chapters.forEach { chapter ->
                        chapter.downloaded = false
                        copyToRealm(chapter, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            } finally {
                realm.close()
            }
        }
    }

    override fun getChapter(chapterId: Long): Observable<ChapterViewModel> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                var chapterVm: ChapterViewModel? = null
                try {
                    val result = realm.query<Chapter>("id == $0", chapterId).first().find()
                    // TODO: ChapterViewModel().create(result) needs to be replaced with proper mapping.
                    result?.let { chapterVm = ChapterViewModel().create(it) } // Placeholder for proper mapping
                } finally {
                    realm.close()
                }
                chapterVm ?: throw NoSuchElementException("Chapter not found with id: $chapterId")
            }
        }
    }

    override fun getChapter(pathLink: String, comicId: Long): Observable<ChapterViewModel> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                var chapterVm: ChapterViewModel? = null
                try {
                    val result = realm.query<Chapter>("chapterPath == $0 AND comic.id == $1", pathLink, comicId).first().find()
                     // TODO: ChapterViewModel().create(result) needs to be replaced with proper mapping.
                    result?.let { chapterVm = ChapterViewModel().create(it) } // Placeholder for proper mapping
                } finally {
                    realm.close()
                }
                chapterVm ?: throw NoSuchElementException("Chapter not found with path: $pathLink, comicId: $comicId")
            }
        }
    }

    // Private helper methods find() and findAll() are removed as their logic is inlined or replaced by new query patterns.
}