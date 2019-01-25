package com.tutorial.bnr.ditzlern.photogallery.gallery.fragments

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.ImageView
import com.tutorial.bnr.ditzlern.photogallery.QueryPreferences

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
        setHasOptionsMenu(true)
        updateItems()
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

    override fun onCreateOptionsMenu(menu: Menu,
                                     inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: ${queryText}")
                    QueryPreferences.setStoredQuery(context, queryText)
                    updateItems()
                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG,"QueryTextChange: ${queryText}")
                    return false
                }
            })

            setOnSearchClickListener {
                val query = QueryPreferences.getStoredQuery(requireContext())
                searchView.setQuery(query, false)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_item_clear -> {
                QueryPreferences.setStoredQuery(requireContext(), "")
                updateItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun updateItems() {
        val query = QueryPreferences.getStoredQuery(requireContext())
        FetchItemsTask(query).execute()
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

    private inner class FetchItemsTask(private val query: String = "") : AsyncTask<Unit, Unit, List<GalleryItem>>() {
        override fun doInBackground(vararg p0: Unit) : List<GalleryItem> {


            if (query.isEmpty()) {
                return FlickrFetchr().fetchInterestingPhotos()
            } else {
                return FlickrFetchr().searchPhotos(query)
            }
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

    private inner class PhotoHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val itemImageView: ImageView = itemView
        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        fun bindDrawable(drawable: Drawable) {
            itemImageView.setImageDrawable(drawable)
        }

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        override fun onClick(v: View) {
            val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
            startActivity(intent)
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

            holder.bindGalleryItem(galleryItem)
            val placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
            holder.bindDrawable(placeholder!!)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }
    }
}