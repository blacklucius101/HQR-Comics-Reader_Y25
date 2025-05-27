package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.IDefaultModelsRepository
import com.tiagohs.hqr.database.ISourceRepository
import com.tiagohs.hqr.factory.DefaultModelFactory
import com.tiagohs.hqr.models.database.DefaultModel
import com.tiagohs.hqr.models.database.SourceDB
package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.IDefaultModelsRepository
import com.tiagohs.hqr.database.ISourceRepository
import com.tiagohs.hqr.factory.DefaultModelFactory
import com.tiagohs.hqr.models.database.DefaultModel
import com.tiagohs.hqr.models.database.SourceDB
import com.tiagohs.hqr.models.view_models.DefaultModelView
import io.reactivex.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.runBlocking
import javax.inject.Inject // Added for Dagger

class DefaultModelsRepository @Inject constructor( // Added @Inject constructor
    private val sourceRepository: ISourceRepository,
    private val realmConfiguration: RealmConfiguration
) : BaseRepository(realmConfiguration), IDefaultModelsRepository {

    override fun insertRealm(defaultModelView: DefaultModelView, sourceId: Long): DefaultModelView {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            var finalModelView = DefaultModelView() // Or copy from input
            try {
                // TODO: sourceRepository.getSourceByIdRealm needs to be updated. Assuming it returns new SourceDB?
                val source: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)

                realm.write {
                    var existingModel = this.query<DefaultModel>("pathLink == $0 AND source.id == $1", defaultModelView.pathLink, sourceId).first().find()

                    if (existingModel != null) {
                        // TODO: DefaultModelFactory.copyFromDefaultModelView needs refactor.
                        // For now, manually map essential fields.
                        existingModel.name = defaultModelView.name
                        // ... other fields ...
                        existingModel.source = source // This linking needs careful handling if source is not from the same realm instance
                        val updatedModel = copyToRealm(existingModel, updatePolicy = UpdatePolicy.ALL)
                        finalModelView.copyFrom(updatedModel, source) // Factory method might need realm instance or just map
                    } else {
                        // TODO: DefaultModel().create() and DefaultModelFactory.copyFromDefaultModelView needs refactor.
                        var newModel = DefaultModel().apply {
                            // Manually map from DefaultModelView to new DefaultModel RealmObject
                            this.id = defaultModelView.id // May need new ID generation
                            this.name = defaultModelView.name
                            this.pathLink = defaultModelView.pathLink
                            this.type = defaultModelView.type
                            this.source = source
                        }
                        val persistedModel = copyToRealm(newModel, updatePolicy = UpdatePolicy.ALL)
                        finalModelView.copyFrom(persistedModel, source)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace() // Log error
            } finally {
                realm.close()
            }
            finalModelView
        }
    }

    override fun insertRealm(defaultModelViewList: List<DefaultModelView>, sourceId: Long): List<DefaultModelView> {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            val finalModelViewList = mutableListOf<DefaultModelView>()
            try {
                // TODO: sourceRepository.getSourceByIdRealm needs to be updated.
                val source: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)

                realm.write {
                    defaultModelViewList.forEach { defaultModelView ->
                        // Simplified logic from single insert, subject to Factory refactor
                        var existingModel = this.query<DefaultModel>("pathLink == $0 AND source.id == $1", defaultModelView.pathLink, sourceId).first().find()
                        if (existingModel != null) {
                            existingModel.name = defaultModelView.name
                            val updatedModel = copyToRealm(existingModel, updatePolicy = UpdatePolicy.ALL)
                            finalModelViewList.add(DefaultModelView().apply { copyFrom(updatedModel, source) })
                        } else {
                            var newModel = DefaultModel().apply {
                                this.id = defaultModelView.id
                                this.name = defaultModelView.name
                                this.pathLink = defaultModelView.pathLink
                                this.type = defaultModelView.type
                                this.source = source
                            }
                            val persistedModel = copyToRealm(newModel, updatePolicy = UpdatePolicy.ALL)
                            finalModelViewList.add(DefaultModelView().apply { copyFrom(persistedModel, source) })
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace() // Log error
            } finally {
                realm.close()
            }
            if (finalModelViewList.isEmpty()) emptyList() else finalModelViewList
        }
    }

    override fun insertOrUpdateComic(defaultModelView: DefaultModelView, sourceId: Long): Observable<DefaultModelView> {
        return Observable.fromCallable { insertRealm(defaultModelView, sourceId) }
    }

    override fun insertOrUpdateComic(defaultModelViewList: List<DefaultModelView>, sourceId: Long): Observable<List<DefaultModelView>> {
        return Observable.fromCallable { insertRealm(defaultModelViewList, sourceId) }
    }

    override fun getAllPublishers(sourceId: Long): Observable<List<DefaultModelView>> {
        return getAllByType(sourceId, DefaultModel.PUBLISHER)
    }

    override fun getAllScanlators(sourceId: Long): Observable<List<DefaultModelView>> {
        return getAllByType(sourceId, DefaultModel.SCANLATOR)
    }

    override fun getAllGenres(sourceId: Long): Observable<List<DefaultModelView>> {
        return getAllByType(sourceId, DefaultModel.GENRE)
    }

    override fun getAllAuthor(sourceId: Long): Observable<List<DefaultModelView>> {
        return getAllByType(sourceId, DefaultModel.AUTHOR)
    }

    private fun getAllByType(sourceId: Long, type: String): Observable<List<DefaultModelView>> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                // TODO: sourceRepository.getSourceByIdRealm needs to be updated.
                val source: SourceDB? = sourceRepository.getSourceByIdRealm(sourceId)
                try {
                    val results = realm.query<DefaultModel>("source.id == $0 AND type == $1", sourceId, type).find()
                    // TODO: DefaultModelFactory.createListOfDefaultModelView needs to be updated
                    DefaultModelFactory.createListOfDefaultModelView(realm.copyFromRealm(results), source)
                } finally {
                    realm.close()
                }
            }
        }
    }

    // Removed findAllDefaultModel as its logic is inlined or adapted into getAllByType
}