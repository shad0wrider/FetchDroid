package com.fetchdroid
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream

class ViewRunner(private val context: Context) : RecyclerView.Adapter<ViewRunner.ImageViewHolder>() {

    // Hardcode raw image resource IDs
    private val imageResources = arrayOf(
        R.raw.coverimage,
        R.raw.coverimage2,
        R.raw.welcome_location_dark,
        R.raw.welcome_ringing_dark,
        R.raw.welcome_save_dark,
        R.raw.welcome_battery_perms_dark,
        R.raw.welcome_location_perms_dark,
        R.raw.feature1,
        R.raw.feature2,
        R.raw.ringfeature
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_view_page, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageResId = imageResources[position]

        // Load raw resource and set as bitmap in the ImageView
        try {
            val inputStream: InputStream = context.resources.openRawResource(imageResId)

            val bitmap = BitmapFactory.decodeStream(inputStream)

            var imageratio = bitmap.width.toFloat() / bitmap.height.toFloat()

            var imgwidth = this.context.resources.displayMetrics.widthPixels

            val newheight = (imgwidth/imageratio).toInt()

            val layoutParams = holder.cardView.layoutParams
            layoutParams.height = newheight
            holder.cardView.layoutParams = layoutParams

            holder.imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return imageResources.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageincard)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }
}
