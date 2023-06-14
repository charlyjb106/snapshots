package com.example.snapshots.entities

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * The model of the Snapshot
 * @param id The snapshot's id
 * @param ownerUid The id of the user who post this snapshot
 * @param title snapshot's title
 * @param photoUrl The post's image
 * @param likeList List of user who likes the post
 */
@IgnoreExtraProperties
data class Snapshot( @get:Exclude var id: String = "",
                     var ownerUid: String = "",
                     var title: String = "",
                     var photoUrl: String = "",
                     var likeList:Map<String, Boolean> = mutableMapOf()
)
