package com.tutorial.bnr.ditzlern.photogallery.gallery.fragments

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.tutorial.bnr.ditzlern.photogallery.R
import com.tutorial.bnr.ditzlern.photogallery.gallery.model.GalleryItem
import com.tutorial.bnr.ditzlern.photogallery.networking.FlickrFetchr
import com.tutorial.bnr.ditzlern.photogallery.networking.ThumbnailDownloader

private const val TAG = "PhotoGalleryFragment"
private var items = emptyList<GalleryItem>()

class PhotoGalleryFragment : Fragment(){

    private lateinit var photoRecyclerView: RecyclerView
    private var items = emptyList<GalleryItem>()
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        FetchItemsTask().execute()
        val responseHandler = Handler()
        thumbnailDownloader =
                ThumbnailDownloader<PhotoHolder>(
                        responseHandler
                ) { photoHolder, bitmap ->
                    val drawable = BitmapDrawable(resources, bitmap)
                    photoHolder.bindDrawable(drawable) }.apply {
                    start()
                    looper }
        Log.i(TAG, "Background thread started")
    }

    override fun onDestroy() {
        super.onDestroy()
        thumbnailDownloader.quit()
        Log.i(TAG, "Background thread destroyed")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        photoRecyclerView =
                view.findViewById(R.id.photo_recycler_View) as RecyclerView
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3) as RecyclerView.LayoutManager?

        setupAdapter()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearQueue()
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

    private class PhotoHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {


        private val itemImageView: ImageView = itemView

        fun bindDrawable(drawable: Drawable) {
            itemImageView.setImageDrawable(drawable)
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>)
        : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): PhotoHolder {

            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView

            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder,
                                      position: Int) {
            val galleryItem = galleryItems[position]

            val placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
            holder.bindDrawable(placeholder!!)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }
    }
}