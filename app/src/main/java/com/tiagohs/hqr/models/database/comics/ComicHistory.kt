package com.tiagohs.hqr.models.database.comics

import com.tiagohs.hqr.models.base.IComicHistory
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ComicHistory : RealmObject, IComicHistory {

    @PrimaryKey
    override var id: Long = -1L

    override var lastTimeRead: String? = ""
    override var comic: Comic? = null
    override var chapter: Chapter? = null

}