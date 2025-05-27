package com.tiagohs.hqr.models.database

import com.tiagohs.hqr.models.base.ISource
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class SourceDB : RealmObject, ISource {

    @PrimaryKey
    override var id: Long = -1L

    override var name: String? = ""
    override var baseUrl: String = ""
    override var language: String = ""

    override var hasThumbnailSupport: Boolean = false

    override var lastPopularUpdate: String? = null
    override var lastLastestUpdate: String? = null

    override var hasInAllPageSupport: Boolean = false
    override var hasInPublisherPageSupport: Boolean = false
    override var hasInScanlatorPageSupport: Boolean = false
    override var hasInGenresPageSupport: Boolean = false

}