package com.tiagohs.hqr.models.database

import com.tiagohs.hqr.models.base.IDefaultModel
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class DefaultModel : RealmObject, IDefaultModel {

    @PrimaryKey
    override var id: Long = -1L

    override var name: String? = "" // Keep as nullable with default, or make non-nullable: String = ""
    override var pathLink: String? = "" // Keep as nullable with default, or make non-nullable: String = ""

    // For linked objects, they should be nullable if they can be absent.
    // The type SourceDB itself needs to be a RealmObject too.
    override var source: SourceDB? = null

    override var type: String? = "" // Keep as nullable with default, or make non-nullable: String = ""

}