package com.usaclean.sportreelzz.model

import android.os.Parcel
import android.os.Parcelable


data class Story(
    var postType: String = "",
    var subject: String = "",
    var chapters: HashMap<String, String> = hashMapOf(),
    var question: String = "",
    var language: String = "",
    var sports: String = "",
    var publishDate: Long = 0,
    var duration: Long = 0,
    var views: Long = 0,
    var userId: String = "",
    var postId: String = "",
    var thumbnailImage: String = "",
    var videoLink: String = "",
    var likedBy: HashMap<String, Boolean> = hashMapOf()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readSerializable() as HashMap<String, String>,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readSerializable() as HashMap<String, Boolean>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postType)
        parcel.writeString(subject)
        parcel.writeString(question)
        parcel.writeString(language)
        parcel.writeString(sports)
        parcel.writeLong(publishDate)
        parcel.writeLong(duration)
        parcel.writeLong(views)
        parcel.writeString(userId)
        parcel.writeString(postId)
        parcel.writeString(thumbnailImage)
        parcel.writeString(videoLink)
        parcel.writeSerializable(likedBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Story> {
        override fun createFromParcel(parcel: Parcel): Story {
            return Story(parcel)
        }

        override fun newArray(size: Int): Array<Story?> {
            return arrayOfNulls(size)
        }
    }

    fun setUserLike(userId: String, isLike :Boolean) {
        likedBy[userId] = isLike
    }

    fun removeLike(userId: String) {
        likedBy.remove(userId)
    }

}
