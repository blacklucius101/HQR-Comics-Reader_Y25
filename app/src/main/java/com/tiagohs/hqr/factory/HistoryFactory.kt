package com.tiagohs.hqr.factory

import com.tiagohs.hqr.models.database.comics.Comic // New RealmObject type
import com.tiagohs.hqr.models.database.comics.Chapter // New RealmObject type
import com.tiagohs.hqr.models.database.comics.ComicHistory // New RealmObject type
import com.tiagohs.hqr.models.view_models.ComicHistoryViewModel
// Removed: import io.realm.Realm

object HistoryFactory {

    /**
     * Creates a new (detached) ComicHistory RealmObject from a ComicHistoryViewModel.
     * Assumes that comicVm and chapterVm within comicHistoryViewModel are already mapped to
     * their respective new RealmObject types (Comic, Chapter) or can be mapped by other factories.
     */
    fun createHistoryFromViewModel(viewModel: ComicHistoryViewModel): ComicHistory {
        return ComicHistory().apply {
            this.id = viewModel.id // Repository should handle ID generation for new items
            this.lastTimeRead = viewModel.lastTimeRead

            // The following assumes ComicsFactory and ChapterFactory now return new Realm model types
            // and that viewModel.comic and viewModel.chapter are ComicViewModel and ChapterViewModel respectively.
            // This part is highly dependent on how ComicViewModel and ChapterViewModel are structured
            // and how their corresponding Realm objects (Comic, Chapter) are obtained or created.
            // For now, we'll assume some mapping or direct assignment if types are compatible.

            viewModel.comic?.let { comicVm ->
                // TODO: This needs proper mapping from ComicViewModel to Comic (RealmObject)
                // For now, if comicVm.source is already a new SourceDB, this might work.
                // This is a placeholder for a more complex mapping that should occur before this factory method.
                // Ideally, the repository would fetch/create managed Comic and Chapter instances.
                // For the factory, it should expect already converted RealmObject types or use other factories.
                // Let's assume comicVm.toRealmObject() or similar exists, or ComicsFactory.createComicFromViewModel is called before.
                // This part is simplified for now.
                val comicRealmObject = Comic().apply { // Placeholder
                     this.id = comicVm.id
                     this.name = comicVm.name
                     this.pathLink = comicVm.pathLink
                     this.source = comicVm.source // Assuming comicVm.source is already a new SourceDB
                }
                this.comic = comicRealmObject

                viewModel.chapter?.let { chapterVm ->
                    // Similar placeholder logic for chapter
                    val chapterRealmObject = Chapter().apply {
                        this.id = chapterVm.id
                        this.chapterName = chapterVm.chapterName
                        this.chapterPath = chapterVm.chapterPath
                        this.comic = comicRealmObject // Link to the above comic
                    }
                    this.chapter = chapterRealmObject
                }
            }
        }
    }

    /**
     * Updates an existing (detached or managed) ComicHistory RealmObject from a ComicHistoryViewModel.
     */
    fun updateHistoryFromViewModel(
        existingHistory: ComicHistory,
        viewModel: ComicHistoryViewModel
    ): ComicHistory {
        existingHistory.lastTimeRead = viewModel.lastTimeRead ?: existingHistory.lastTimeRead

        // Similar complexities for updating linked comic and chapter as in createHistoryFromViewModel.
        // The repository should handle fetching/updating these linked objects.
        // This factory method should ideally receive already mapped/updated Comic and Chapter RealmObjects.
        // For now, direct assignment if fields are simple, or TODOs for complex parts.

        viewModel.comic?.let { comicVm ->
            if (existingHistory.comic == null) existingHistory.comic = Comic() // Create if null
            existingHistory.comic?.apply { // Apply updates to existing or new comic
                this.id = comicVm.id
                this.name = comicVm.name
                // ... other comic fields ...
                this.source = comicVm.source
            }

            viewModel.chapter?.let { chapterVm ->
                if (existingHistory.chapter == null) existingHistory.chapter = Chapter()
                existingHistory.chapter?.apply {
                    this.id = chapterVm.id
                    this.chapterName = chapterVm.chapterName
                    // ... other chapter fields ...
                    this.comic = existingHistory.comic // Ensure link
                }
            }
        }


        return existingHistory
    }

    /**
     * Maps a ComicHistory RealmObject (new SDK) to a ComicHistoryViewModel.
     * Relies on ComicHistoryViewModel's own mapping logic.
     */
    fun createComicHistoryViewModel(comicHistory: ComicHistory): ComicHistoryViewModel {
        return ComicHistoryViewModel().create(comicHistory) // Assuming .create() is updated for new ComicHistory
    }

    /**
     * Maps a list of ComicHistory RealmObjects (new SDK) to a list of ComicHistoryViewModels.
     */
    fun createListOfComicHistoryViewModel(comicHistories: List<ComicHistory>): List<ComicHistoryViewModel> {
        return comicHistories.map { createComicHistoryViewModel(it) }
    }
}