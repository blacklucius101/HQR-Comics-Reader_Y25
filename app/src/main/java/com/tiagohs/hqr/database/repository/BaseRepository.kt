package com.tiagohs.hqr.database.repository

import io.reactivex.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.runBlocking

abstract class BaseRepository(
    // TODO: This configuration needs to be provided by Dagger.
    // For now, we assume it will be passed or accessed globally from App.kt (though not ideal)
    // This is a placeholder for where the configuration would come from.
    // A proper solution would involve Dagger providing this.
    protected val realmConfiguration: RealmConfiguration // Changed to protected
) {

    protected inline fun <reified T : RealmObject> insert(data: T): Observable<T> {
        return Observable.fromCallable {
            runBlocking { // Bridge to suspend world for Observable
                val realm = Realm.open(realmConfiguration)
                try {
                    realm.write {
                        copyToRealm(data, updatePolicy = UpdatePolicy.ALL)
                    }
                } finally {
                    realm.close()
                }
            }
            data // Return the original data after transaction
        }
    }

    protected fun <T : RealmObject> insert(data: List<T>): Observable<List<T>> {
        return Observable.fromCallable {
            runBlocking { // Bridge to suspend world for Observable
                val realm = Realm.open(realmConfiguration)
                try {
                    realm.write {
                        data.forEach { item ->
                            copyToRealm(item, updatePolicy = UpdatePolicy.ALL)
                        }
                    }
                } finally {
                    realm.close()
                }
            }
            data // Return the original list after transaction
        }
    }

import io.realm.kotlin.ext.query // Added for query<T>()

// ... (rest of the class from previous turn)

    protected inline fun <reified T : RealmObject> deleteAll(): Observable<Unit> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    realm.write {
                        val results = this.query<T>().find()
                        delete(results)
                    }
                } finally {
                    realm.close()
                }
            }
        }
    }

    protected inline fun <reified T : RealmObject> delete(id: Long): Observable<Unit> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    realm.write {
                        this.query<T>("id == $0", id).first().find()?.also {
                            delete(it)
                        }
                    }
                } finally {
                    realm.close()
                }
            }
        }
    }

    protected inline fun <reified T : RealmObject> delete(itemsId: List<Long>): Observable<Unit> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    realm.write {
                        itemsId.forEach { id ->
                            this.query<T>("id == $0", id).first().find()?.also {
                                delete(it)
                            }
                        }
                    }
                } finally {
                    realm.close()
                }
            }
        }
    }

}