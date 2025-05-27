package com.tiagohs.hqr.database.repository

import com.tiagohs.hqr.database.ISourceRepository
import com.tiagohs.hqr.models.database.CatalogueSource
import com.tiagohs.hqr.models.database.SourceDB
import io.reactivex.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.runBlocking
import javax.inject.Inject // Added for Dagger

class SourceRepository @Inject constructor( // Added @Inject constructor
    private val realmConfiguration: RealmConfiguration
) : BaseRepository(realmConfiguration), ISourceRepository {

    override fun insertSourceRealm(source: SourceDB): SourceDB {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                realm.write {
                    copyToRealm(source, updatePolicy = UpdatePolicy.ALL)
                }
            } finally {
                realm.close()
            }
            source // Return the original persisted object
        }
    }

    override fun insertSource(sourceDB: SourceDB): Observable<SourceDB> {
        // This uses the insert method from BaseRepository, which is already refactored.
        return super.insert(sourceDB)
    }

    override fun getAllSources(): Observable<List<SourceDB>> { // Return type non-nullable List
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val results = realm.query<SourceDB>().find()
                    realm.copyFromRealm(results) // Detach from Realm for use in RxJava stream
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getAllCatalogueSources(): Observable<List<CatalogueSource>> { // Return type non-nullable List
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val results = realm.query<CatalogueSource>().find()
                    realm.copyFromRealm(results)
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getCatalogueSourceById(catalogueSourceId: Long): Observable<CatalogueSource?> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val result = realm.query<CatalogueSource>("id == $0", catalogueSourceId).first().find()
                    result?.let { realm.copyFromRealm(it) }
                } finally {
                    realm.close()
                }
            }
        }
    }

    override fun getSourceByIdRealm(sourceId: Long): SourceDB? {
        return runBlocking {
            val realm = Realm.open(realmConfiguration)
            try {
                val result = realm.query<SourceDB>("id == $0", sourceId).first().find()
                result?.let { realm.copyFromRealm(it) } // Detach if found
            } finally {
                realm.close()
            }
        }
    }

    override fun getSourceById(sourceId: Long): Observable<SourceDB?> {
        return Observable.fromCallable {
            runBlocking {
                val realm = Realm.open(realmConfiguration)
                try {
                    val result = realm.query<SourceDB>("id == $0", sourceId).first().find()
                    result?.let { realm.copyFromRealm(it) }
                } finally {
                    realm.close()
                }
            }
        }
    }

}