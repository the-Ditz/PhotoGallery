package com.tutorial.bnr.ditzlern.photogallery.gallery.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tutorial.bnr.ditzlern.photogallery.R
import com.tutorial.bnr.ditzlern.photogallery.gallery.model.GalleryItem
import com.tutorial.bnr.ditzlern.photogallery.networking.FlickrFetchr

private const val TAG = "PhotoGalleryFragment"
private var items = emptyList<GalleryItem>()

class PhotoGalleryFragment : Fragment(){

    private lateinit var photoRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        FetchItemsTask().execute()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        photoRecyclerView =
                view.findViewById(R.id.photo_recycler_View) as RecyclerView
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        setupAdapter()

        return view
    }

    private inner class FetchItemsTask() : AsyncTask<Unit, Unit, List<GalleryItem>>() {
        override fun doInBackground(vararg p0: Unit) : List<GalleryItem> {
            return FlickrFetchr().fetchItems()
        }

        override fun onPostExecute(galleryItems: List<GalleryItem>) {
            items = galleryItems
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        if (isAdded) {
            photoRecyclerView.adapter = PhotoAdapter(items)
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }

    private class PhotoHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView) {
        private val itemTextView: TextView = itemView;

        fun bindGalleryItem(galleryItem: GalleryItem) {
            itemTextView.text = galleryItem.title
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>)
        : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): PhotoHolder {
            val textView = TextView(activity)
            return PhotoHolder(textView)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder,
                                      position: Int) {
            val galleryItem = galleryItems[position]
            holder.bindGalleryItem(galleryItem)
        }
    }
}