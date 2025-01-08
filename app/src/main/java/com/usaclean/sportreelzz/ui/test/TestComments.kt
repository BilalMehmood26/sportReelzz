package com.usaclean.sportreelzz.ui.test

import android.os.Parcel
import android.os.Parcelable
import com.usaclean.sportreelzz.model.Comments
import java.util.ArrayList

data class TestComments(
    var commentId: String = "",
    var content: String = "",
    var fromId: String = "",
    var timeStamp: Long = 0,
    var videoTimeStamp: Long = 0,
    var toId: String = "",
    var videoLink: String = "",
    var type: String = "",
    var likedBy: HashMap<String, Boolean> = hashMapOf(),
    var reply: ArrayList<TestComments>? = arrayListOf()
) : Parcelable {

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
        parcel.createTypedArrayList(CREATOR)
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

    companion object CREATOR : Parcelable.Creator<TestComments> {
        override fun createFromParcel(parcel: Parcel): TestComments {
            return TestComments(parcel)
        }

        override fun newArray(size: Int): Array<TestComments?> {
            return arrayOfNulls(size)
        }
    }
}

