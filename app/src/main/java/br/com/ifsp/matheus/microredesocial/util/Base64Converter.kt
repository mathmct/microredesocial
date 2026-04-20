package br.com.ifsp.matheus.microredesocial.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

class Base64Converter {
    companion object {
        fun drawableToString(drawable: Drawable?): String {
            if (drawable == null) return ""
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            val scaledBitmap = bitmap.scale(150, 150)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            return imageString
        }

        fun stringToBitmap(imageString: String): Bitmap {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            return decodedImage
        }
    }
}