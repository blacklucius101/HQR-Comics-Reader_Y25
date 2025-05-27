package com.tiagohs.hqr.models.database

import com.tiagohs.hqr.models.base.ICatalogueSource
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class CatalogueSource : RealmObject, ICatalogueSource<SourceDB> {

    @PrimaryKey
    override var id: Long = -1L
    override var language: String? = null
    override var sourceDBS: RealmList<SourceDB> = realmListOf()

}