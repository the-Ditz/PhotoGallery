package com.tutorial.bnr.ditzlern.photogallery.gallery.model

import android.net.Uri

data class GalleryItem(var title: String = "",
                       var id: String = "",
                       var url: String = "",
                       var owner: String = "") {
    val photoPageUri: Uri
    get() {
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
    }
}