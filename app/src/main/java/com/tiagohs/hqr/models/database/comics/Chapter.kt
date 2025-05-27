package com.tiagohs.hqr.models.database.comics

import com.tiagohs.hqr.models.base.IChapter
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Chapter : RealmObject, IChapter {

    @PrimaryKey
    override var id: Long = -1L

    override var chapterName: String? = ""
    override var chapterPath: String? = ""
    override var lastPageRead: Int = -1
    override var downloaded: Boolean = false

    // The linked object Comic also needs to be a RealmObject.
    // It should be nullable if it can be absent.
    override var comic: Comic? = null

}