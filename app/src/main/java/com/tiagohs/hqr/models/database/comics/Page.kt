package com.tiagohs.hqr.models.database.comics

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Page : RealmObject {

    @PrimaryKey
    var id: Long = -1L
    var index: Int? = 0
    var url: String? = ""
    var imageUrl: String? = null

    // The Realm Kotlin SDK requires a default constructor or for all properties to be initialized.
    // Adding a default constructor for clarity if no other constructors are defined.
    constructor() {}

    // Example of a constructor if needed, ensuring all properties are handled.
    // constructor(id: Long = -1L, index: Int? = 0, url: String? = "", imageUrl: String? = null) {
    //     this.id = id
    //     this.index = index
    //     this.url = url
    //     this.imageUrl = imageUrl
    // }
}