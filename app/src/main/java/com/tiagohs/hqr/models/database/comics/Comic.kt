package com.tiagohs.hqr.models.database.comics

import com.tiagohs.hqr.models.base.IComic
import com.tiagohs.hqr.models.database.DefaultModel
import com.tiagohs.hqr.models.database.SourceDB
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Comic : RealmObject, IComic {

    @PrimaryKey
    override var id: Long = -1L

    override var name: String? = ""
    override var pathLink: String? = ""
    override var posterPath: String? = ""
    override var summary: String? = ""
    override var publicationDate: String? = ""

    override var publisher: RealmList<DefaultModel> = realmListOf()
    override var genres: RealmList<DefaultModel> = realmListOf()
    override var authors: RealmList<DefaultModel> = realmListOf()
    override var scanlators: RealmList<DefaultModel> = realmListOf()
    override var chapters: RealmList<Chapter> = realmListOf()

    override var downloaded: Boolean = false
    override var inicialized: Boolean = false
    override var favorite: Boolean = false
    override var lastUpdate: String? = ""

    override var status: String? = ""
    // Custom setter logic for 'status' needs to be handled outside the model class in the new SDK
    // e.g., when mapping from a ViewModel or a DTO.
    // set(value) {
    //     field = ScreenUtils.getStatusConstant(value)
    // }

    override var tags: RealmList<String> = realmListOf()

    override var source: SourceDB? = null

}