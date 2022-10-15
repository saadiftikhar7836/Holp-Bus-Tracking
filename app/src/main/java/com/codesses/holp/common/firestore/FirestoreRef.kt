/*
 *
 * Created by Saad Iftikhar
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.codesses.holp.common.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRef {

    //   Database instance
    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    //    Driver reference
    fun getDriversRef(androidId: String): CollectionReference {
        return getInstance()
            .collection("bus_details")
            .document("driver")
            .collection(androidId)
    }

    fun getActiveBus(): CollectionReference {
        return getInstance()
            .collection("bus_details")
            .document("active_bus")
            .collection("bus_1")
    }

    fun getActiveBus(busId: String): DocumentReference {
        return getInstance().collection("active_buses").document(busId)
    }

    fun getTripsRef(): CollectionReference {
        return getInstance()
            .collection("trips")
    }

    //    User reference
    fun getCharacterRequestsRef(): CollectionReference {
        return getInstance().collection("character_requests")
    }

    //    Character reference
    fun getCharacterRef(): CollectionReference {
        return getInstance().collection("characters")
    }

    fun getVotesRef(characterId: String): CollectionReference {
        return getCharacterRef().document(characterId)
            .collection("votes")
    }

    fun getFollowersRef(userId: String): CollectionReference {
        return getInstance().collection("followers_following")
            .document("followers")
            .collection(userId)
    }

    fun getFollowingRef(userId: String): CollectionReference {
        return getInstance().collection("followers_following")
            .document("following")
            .collection(userId)
    }

    fun getFavouriteRef(): CollectionReference {
        return getInstance().collection("favourite_characters")
    }

    fun getRecentVotesRef(): CollectionReference {
        return getInstance().collection("recent_votes")
    }

    fun getCharacterCommentsRef(characterId: String): CollectionReference {
        return getInstance().collection("comments")
            .document("character_comments")
            .collection(characterId)
//            .collection()
    }

    fun getCommentsReplyRef(characterId: String, commentId: String): CollectionReference {
        return getCharacterCommentsRef(characterId)
            .document(commentId).collection("replies")
//            .collection()
    }

    fun getCommentLikesRef(characterId: String, commentId: String): CollectionReference {
        return getCharacterCommentsRef(characterId).document(commentId)
            .collection("likes")
    }

    fun getCommentReplyLikesRef(
        characterId: String,
        commentId: String,
        replyId: String
    ): CollectionReference {
        return getCommentsReplyRef(characterId, commentId)
            .document(replyId).collection("likes")
    }

}
