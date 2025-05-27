package com.tiagohs.hqr.factory

// Removed: import com.tiagohs.hqr.helpers.tools.RealmUtils
import com.tiagohs.hqr.models.database.SourceDB // New RealmObject type
import com.tiagohs.hqr.models.database.comics.Comic // New RealmObject type
import com.tiagohs.hqr.models.view_models.ChapterViewModel
import com.tiagohs.hqr.models.view_models.ComicViewModel
import com.tiagohs.hqr.models.view_models.DefaultModelView
// Removed: import io.realm.Realm
// Removed: import io.realm.RealmList
import io.realm.kotlin.ext.realmListOf // For new RealmList

object ComicsFactory {

    /**
     * Creates a new (detached) Comic RealmObject from a ComicViewModel.
     * References to other RealmObjects (SourceDB, DefaultModel, Chapter) are assumed to be
     * either already converted to their new RealmObject forms or will be handled by repository
     * during persistence. This factory focuses on direct property mapping.
     * ID assignment is taken from ViewModel; repository handles new ID generation if comicVm.id is default.
     */
    fun createComicFromViewModel(comicVm: ComicViewModel, sourceDbRef: SourceDB?): Comic {
        return Comic().apply {
            this.id = comicVm.id
            this.name = comicVm.name
            this.posterPath = comicVm.posterPath
            this.pathLink = comicVm.pathLink
            this.summary = comicVm.summary
            this.publicationDate = comicVm.publicationDate
            this.lastUpdate = comicVm.lastUpdate
            this.status = comicVm.status // Status mapping (e.g., ScreenUtils.getStatusConstant) should be done before calling this factory or in ViewModel
            this.favorite = comicVm.favorite
            this.downloaded = comicVm.downloaded
            this.inicialized = comicVm.inicialized

            this.source = sourceDbRef

            // For lists, map from ViewModel lists to new RealmObject lists.
            // This assumes DefaultModelFactory and ChapterFactory are updated and return new RealmObject types.
            // The repository will handle persisting these lists.
            comicVm.publisher?.let { vms -> this.publisher.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef)) }
            comicVm.genres?.let { vms -> this.genres.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef)) }
            comicVm.authors?.let { vms -> this.authors.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef)) }
            comicVm.scanlators?.let { vms -> this.scanlators.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef)) }
            
            // For chapters, we need the 'this' comic as a reference for ChapterFactory
            // This creates a slight chicken-and-egg if Chapter needs Comic ID immediately.
            // For now, pass 'this' (unmanaged Comic) as comicReference.
            comicVm.chapters?.let { vms -> this.chapters.addAll(ChapterFactory.createChapterListFromViewModelList(vms, this)) }

            comicVm.tags?.let { tagsVm -> this.tags.addAll(tagsVm) }
        }
    }

    /**
     * Updates an existing (detached or managed) Comic RealmObject from a ComicViewModel.
     */
    fun updateComicFromViewModel(
        existingComic: Comic,
        comicVm: ComicViewModel,
        sourceDbRef: SourceDB?,
        skipFavorite: Boolean? = false
    ): Comic {
        existingComic.name = comicVm.name ?: existingComic.name
        existingComic.posterPath = comicVm.posterPath ?: existingComic.posterPath
        existingComic.pathLink = comicVm.pathLink ?: existingComic.pathLink
        existingComic.summary = comicVm.summary ?: existingComic.summary
        existingComic.publicationDate = comicVm.publicationDate ?: existingComic.publicationDate
        existingComic.lastUpdate = comicVm.lastUpdate ?: existingComic.lastUpdate
        existingComic.status = comicVm.status ?: existingComic.status // status mapping done elsewhere

        if (sourceDbRef != null) {
            existingComic.source = sourceDbRef
        }

        // TODO: More sophisticated list update logic might be needed (clear and add, or diffing).
        // For now, assuming replacement or that the repository handles merging.
        comicVm.publisher?.let { vms ->
            existingComic.publisher.clear()
            existingComic.publisher.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef))
        }
        comicVm.genres?.let { vms ->
            existingComic.genres.clear()
            existingComic.genres.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef))
        }
        comicVm.authors?.let { vms ->
            existingComic.authors.clear()
            existingComic.authors.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef))
        }
        comicVm.scanlators?.let { vms ->
            existingComic.scanlators.clear()
            existingComic.scanlators.addAll(DefaultModelFactory.createListFromViewModelList(vms, sourceDbRef))
        }
        comicVm.chapters?.let { vms ->
            existingComic.chapters.clear()
            existingComic.chapters.addAll(ChapterFactory.createChapterListFromViewModelList(vms, existingComic))
        }
        comicVm.tags?.let { tagsVm ->
            existingComic.tags.clear()
            existingComic.tags.addAll(tagsVm)
        }

        if (skipFavorite != true) {
            existingComic.favorite = comicVm.favorite
            existingComic.downloaded = comicVm.downloaded
        }
        existingComic.inicialized = comicVm.inicialized

        return existingComic
    }

    /**
     * Maps a Comic RealmObject (new SDK, assumed detached or managed) to a ComicViewModel.
     * Relies on ViewModel's own mapping logic for nested objects if present.
     */
    fun createComicViewModel(comicDb: Comic): ComicViewModel {
        return ComicViewModel().apply {
            this.id = comicDb.id
            this.name = comicDb.name
            this.pathLink = comicDb.pathLink
            
            // Assuming SourceDB also has a mapping to its ViewModel or is directly usable
            // This requires SourceDB to be refactored if it had a .create() method like others.
            // For now, direct assignment or placeholder.
            comicDb.source?.let { s -> this.source = s } // Direct assign, may need mapping

            this.posterPath = comicDb.posterPath
            this.summary = comicDb.summary
            this.publicationDate = comicDb.publicationDate

            this.publisher = comicDb.publisher.map { DefaultModelView().create(it, comicDb.source) } // Assumes DefaultModelView.create is updated
            this.genres = comicDb.genres.map { DefaultModelView().create(it, comicDb.source) }
            this.authors = comicDb.authors.map { DefaultModelView().create(it, comicDb.source) }
            this.scanlators = comicDb.scanlators.map { DefaultModelView().create(it, comicDb.source) }
            
            this.chapters = comicDb.chapters.map { ChapterViewModel().create(it) } // Assumes ChapterViewModel.create is updated

            this.tags = realmListOf() // New RealmList
            this.tags.addAll(comicDb.tags)


            this.lastUpdate = comicDb.lastUpdate
            this.status = comicDb.status
            this.favorite = comicDb.favorite
            this.inicialized = comicDb.inicialized
            this.downloaded = comicDb.downloaded
        }
    }

    /**
     * Maps a list of ComicViewModel objects to a list of new (detached) Comic RealmObjects.
     */
    fun createComicListFromViewModelList(comicViewModels: List<ComicViewModel>?, sourceDbRef: SourceDB?): List<Comic> {
        if (comicViewModels == null) return emptyList()
        return comicViewModels.map { createComicFromViewModel(it, sourceDbRef) }
    }

    /**
     * Maps a list of Comic RealmObjects (new SDK) to a list of ComicViewModels.
     */
    fun createListOfComicViewModel(comicsDb: List<Comic>): List<ComicViewModel> {
        return comicsDb.map { createComicViewModel(it) }
    }

    // createListOfTagsForRealm is no longer needed as RealmList<String> can be directly assigned from List<String>
    // in the new model (e.g., comic.tags.addAll(listOfStrings))

}