package com.usaclean.sportreelzz.model

import android.os.Parcel
import android.os.Parcelable

data class Comment(
    var commentId: String = "",
    var content: String = "",
    var fromId: String = "",
    var timeStamp: Long = 0,
    var videoTimeStamp: Long = 0,
    var toId: String = "",
    var videoLink: String = "",
    var type: String = "",
    var likedBy: HashMap<String, Boolean> = hashMapOf(),
    var reply: ArrayList<Reply>? = null
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readSerializable() as HashMap<String, Boolean>,
        parcel.createTypedArrayList(Reply.CREATOR)
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(commentId)
        parcel.writeString(content)
        parcel.writeString(fromId)
        parcel.writeLong(timeStamp)
        parcel.writeLong(videoTimeStamp)
        parcel.writeString(toId)
        parcel.writeString(type)
        parcel.writeString(videoLink)
        parcel.writeSerializable(likedBy)
        parcel.writeTypedList(reply)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comments> {
        override fun createFromParcel(parcel: Parcel): Comments {
            return Comments(parcel)
        }

        override fun newArray(size: Int): Array<Comments?> {
            return arrayOfNulls(size)
        }
    }
}
