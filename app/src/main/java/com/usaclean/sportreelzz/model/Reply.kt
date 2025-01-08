package com.usaclean.sportreelzz.model

import android.os.Parcel
import android.os.Parcelable

data class Reply(
    var commentId: String = "",
    var content: String = "",
    var fromId: String = "",
    var timeStamp: Long = 0,
    var videoTimeStamp: Long = 0,
    var toId: String = "",
    var videoLink: String = "",
    var type: String = "",
    var likedBy: HashMap<String, Boolean> = hashMapOf(),
) : Parcelable {
    companion object CREATOR : Parcelable.Creator<Reply> {
        override fun createFromParcel(parcel: Parcel): Reply {
            return Reply(parcel)
        }

        override fun newArray(size: Int): Array<Reply?> {
            return arrayOfNulls(size)
        }
    }

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
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(commentId)
        parcel.writeString(content)
        parcel.writeString(fromId)
        parcel.writeLong(timeStamp)
        parcel.writeLong(videoTimeStamp)
        parcel.writeString(toId)
        parcel.writeString(type)
        parcel.writeString(videoLink)
        parcel.writeSerializable(likedBy)
    }
}
