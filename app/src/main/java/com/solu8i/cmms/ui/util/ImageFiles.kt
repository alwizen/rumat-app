package com.solu8i.cmms.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Helper bersama untuk mengambil file gambar dari galeri/kamera
 * dan mengubah bitmap kamera menjadi Uri.
 */
object ImageFiles {

    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 80

    /**
     * Salin gambar dari Uri menjadi File JPEG terkompresi di cache.
     * Mengembalikan null bila gambar tidak bisa dibaca.
     */
    fun fromUri(context: Context, uri: Uri): File? {
        return try {
            val bitmap = decodeSampled(context, uri) ?: return null

            val scaled = scaleDown(bitmap)

            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)

            if (scaled !== bitmap) {
                scaled.recycle()
            }
            bitmap.recycle()

            val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
            tempFile.writeBytes(baos.toByteArray())

            if (tempFile.length() == 0L) {
                tempFile.delete()
                return null
            }

            tempFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decode gambar dengan inSampleSize agar tidak menghabiskan memori
     * sebelum diperkecil ke MAX_DIMENSION.
     */
    private fun decodeSampled(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        }

        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var largest = maxOf(width, height)

        while (largest / 2 >= MAX_DIMENSION) {
            largest /= 2
            sample *= 2
        }

        return sample
    }

    /**
     * Ubah bitmap hasil kamera menjadi Uri yang bisa dibaca ulang.
     */
    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)

        if (largest <= MAX_DIMENSION) {
            return bitmap
        }

        val ratio = MAX_DIMENSION.toFloat() / largest
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
