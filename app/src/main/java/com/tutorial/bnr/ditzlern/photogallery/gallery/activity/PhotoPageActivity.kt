package com.tutorial.bnr.ditzlern.photogallery.gallery.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import com.tutorial.bnr.ditzlern.photogallery.gallery.fragments.PhotoPageFragment

class PhotoPageActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        return PhotoPageFragment.newInstance(intent.data)
    }

    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent {
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}