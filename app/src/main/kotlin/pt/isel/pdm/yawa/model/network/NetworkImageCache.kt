package pt.isel.pdm.yawa.model.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.android.volley.toolbox.ImageLoader
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Created by ncaro on 12/15/2016.
 */

interface IImageCache : ImageLoader.ImageCache{
    /**
     * Clears file cache
     * */
    fun clear():Unit
}

/**
 * Uses file system cache dir for better android storage management
 * */
class BitmapFilesystemImageCache(val context: Context) : StubFileCache() {

    override fun getBitmap(url: String):Bitmap? {
        try {
            val fileName = getFileName(url)
            Log.i(TAG, "Loading bitmap from file $fileName")
            val mFile = File(context.cacheDir, fileName)
            if(mFile.exists()){
                FileInputStream(mFile).use { //Try with resources
                    Log.d(TAG,"File $fileName found on cache, decoding to bitmap.")
                    val bitmap = BitmapFactory.decodeStream(it)
                    return bitmap
                }
            }
            Log.d(TAG,"File $fileName not found on cache.")
        } catch(e: Exception) {
            Log.e(TAG, "Error loading file from image cache.", e)
        }
        return null
    }

    override fun putBitmap(url: String, bitmap: Bitmap) {
        try {
            //TODO check if overrides file
            val fileName = getFileName(url)
            Log.i(TAG, "Saving bitmap to file $fileName")
            val mFile = File(context.cacheDir, fileName)
            FileOutputStream(mFile).use { //Try with resources
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch(e: Exception) {
            Log.e(TAG, "Error saving file to image cache.", e)
        }
    }

    override fun clear() {
        try {
            Log.i(TAG, "Clearing cache image files with prefix $TAG")
            context.cacheDir
                    .listFiles(FileFilter { it.startsWith(CACHE_PREFIX) })
                    ?.forEach {
                        it?.delete()
                    }
        } catch(e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
    companion object{
        private const val TAG = "BitmapCache"
        private const val SLASH = '/'
        private const val CACHE_PREFIX = "CACHE_IMG_"
        private fun getFileName(uri:String):String {
            if(uri.lastIndexOf(SLASH)<0) throw IllegalArgumentException("Invalid URI, $uri")
            return CACHE_PREFIX + uri.substringAfterLast(SLASH)
        }
    }

}

open class StubFileCache : IImageCache{
    override fun clear() {

    }

    override fun getBitmap(url: String): Bitmap? {
        return null
    }

    override fun putBitmap(url: String, bitmap: Bitmap) {

    }
}