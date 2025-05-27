package com.tiagohs.hqr.factory

// Removed: import com.tiagohs.hqr.helpers.tools.RealmUtils
import com.tiagohs.hqr.models.database.DefaultModel // New RealmObject type
import com.tiagohs.hqr.models.database.SourceDB // New RealmObject type
import com.tiagohs.hqr.models.view_models.DefaultModelView
// Removed: import io.realm.Realm
// Removed: import io.realm.RealmList
import io.realm.kotlin.types.RealmList // For the parameter type of createStringList if it's from new RealmList

object DefaultModelFactory {

    /**
     * Creates a new (detached) DefaultModel RealmObject from a DefaultModelView.
     * The sourceDbRef is for linking.
     */
    fun createModelFromViewModel(viewModel: DefaultModelView, sourceDbRef: SourceDB?): DefaultModel {
        return DefaultModel().apply {
            this.id = viewModel.id // Assuming ID is managed or set if new by repository
            this.name = viewModel.name
            this.pathLink = viewModel.pathLink
            this.type = viewModel.type
            this.source = sourceDbRef
        }
    }

    /**
     * Updates an existing (detached or managed) DefaultModel RealmObject from a DefaultModelView.
     */
    fun updateModelFromViewModel(
        existingModel: DefaultModel,
        viewModel: DefaultModelView,
        sourceDbRef: SourceDB?
    ): DefaultModel {
        existingModel.name = viewModel.name ?: existingModel.name
        existingModel.pathLink = viewModel.pathLink ?: existingModel.pathLink
        existingModel.type = viewModel.type ?: existingModel.type
        if (sourceDbRef != null) {
            existingModel.source = sourceDbRef
        }
        return existingModel
    }

    /**
     * Maps a list of DefaultModelView objects to a list of new (detached) DefaultModel RealmObjects.
     */
    fun createListFromViewModelList(
        viewModels: List<DefaultModelView>?,
        sourceDbRef: SourceDB?
    ): List<DefaultModel> {
        if (viewModels == null) return emptyList()
        return viewModels.map { viewModel ->
            createModelFromViewModel(viewModel, sourceDbRef)
        }
    }

    /**
     * Maps a list of DefaultModel RealmObjects (new SDK) to a list of DefaultModelViews.
     * Relies on DefaultModelView's own mapping logic (e.g., a constructor or an init/create method).
     */
    fun createListOfDefaultModelView(
        models: List<DefaultModel>?,
        sourceDbRef: SourceDB? /* Source might be needed for context in ViewModel creation */
    ): List<DefaultModelView> {
        if (models == null) return emptyList()
        return models.map { model ->
            DefaultModelView().create(model, sourceDbRef) // Assuming DefaultModelView has a 'create' method
        }
    }

    /**
     * Converts a RealmList<String> (from new SDK) to a standard Kotlin List<String>.
     * This is useful for mapping list properties from managed RealmObjects to ViewModels.
     */
    fun toStringList(realmList: RealmList<String>?): List<String> {
        return realmList?.toList() ?: emptyList()
    }

}