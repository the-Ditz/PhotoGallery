package com.tutorial.bnr.ditzlern.photogallery.networking

import android.net.Uri
import android.util.Log
import com.tutorial.bnr.ditzlern.photogallery.gallery.model.GalleryItem
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "FlickrFetchr"

private val METHOD_KEY = "method"
private const val FETCH_RECENTS_METHOD = "flickr.photos.getRecent" //risk of nsfw
private const val FETCH_INTERESTINGNESS_METHOD = "flickr.interestingness.getList"

private val API_KEY_KEY = "api_key"
private const val API_KEY_VALUE = "4f721bbafa75bf6d2cb5af54f937bb70"

private val ENDPOINT: Uri = Uri.parse("https://api.flickr.com/services/rest/")
        .buildUpon()
        .appendQueryParameter(METHOD_KEY, FETCH_INTERESTINGNESS_METHOD)
        .appendQueryParameter(API_KEY_KEY, API_KEY_VALUE)
        .appendQueryParameter("format", "json")
        .appendQueryParameter("nojsoncallback", "1")
        .appendQueryParameter("extras", "url_s").build()


class FlickrFetchr {

    @Throws(IOException::class)
    fun getUrlBytes(urlSpec: String) : ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpURLConnection
        try {
            val input = connection.inputStream
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("${connection.responseMessage}: with $urlSpec")
            }
            return input.readBytes()
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    fun getUrlString(urlSpec: String) = String(getUrlBytes(urlSpec))

    fun fetchItems(): List<GalleryItem> {
        val items = mutableListOf<GalleryItem>()

        try {
            val url = ENDPOINT.toString()
            val jsonString = getUrlString(url)
            Log.i(TAG, "Received JSON: $jsonString")
            val jsonBody = JSONObject(jsonString)
            parseItems(items, jsonBody)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to fetch items", e)
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse JSON", e)
        }
        return items
    }

    @Throws(IOException:: class, JSONException::class)
    private fun parseItems(items: MutableList<GalleryItem>, jsonBody: JSONObject) {
        val photosJsonObject = jsonBody.getJSONObject("photos")
        val photoJsonArray = photosJsonObject.getJSONArray("photo")

        for (i in 0 until photoJsonArray.length()) {
            val photoJsonObject = photoJsonArray.getJSONObject(i)

            if (!photoJsonObject.has("url_s")) {
                continue
            }

            val item = GalleryItem()
            item.id = photoJsonObject.getString("id")
            item.title = photoJsonObject.getString("title")
            item.url = photoJsonObject.getString("url_s")
            items.add(item)
        }
    }
}