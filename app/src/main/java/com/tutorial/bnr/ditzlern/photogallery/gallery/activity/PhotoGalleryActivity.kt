package com.tutorial.bnr.ditzlern.photogallery.gallery.activity

import android.support.v4.app.Fragment
import com.tutorial.bnr.ditzlern.photogallery.gallery.fragments.PhotoGalleryFragment

class PhotoGalleryActivity : SingleFragmentActivity() {


    override fun createFragment(): Fragment = PhotoGalleryFragment.newInstance()

}
