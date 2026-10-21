package com.vk.starscriptmusic.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.vk.starscriptmusic.core.theme.DeepSpaceCharcoal
import com.vk.starscriptmusic.core.theme.NeoCyan
import com.vk.starscriptmusic.core.theme.NeoViolet
import com.vk.starscriptmusic.core.theme.OledBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class DynamicPaletteColors(
    val primaryAccent: Color = NeoViolet,
    val secondaryAccent: Color = NeoCyan,
    val darkBackdrop: Color = DeepSpaceCharcoal
)

/**
 * Utility to extract vibrant neo-groove chameleon colors from album artwork using Android Palette.
 */
object PaletteExtractor {

    suspend fun extractColors(context: Context, artUriString: String?): DynamicPaletteColors = withContext(Dispatchers.IO) {
        if (artUriString.isNullOrBlank()) {
            return@withContext DynamicPaletteColors()
        }

        try {
            val uri = Uri.parse(artUriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = inputStream?.use { BitmapFactory.decodeStream(it) }

            if (originalBitmap != null) {
                // Scale down bitmap to 120x120 for instant, lightweight palette generation
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 120, 120, true)
                val palette = Palette.from(scaledBitmap).generate()

                val vibrant = palette.vibrantSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                    ?: palette.darkVibrantSwatch?.rgb

                val secondary = palette.lightVibrantSwatch?.rgb
                    ?: palette.mutedSwatch?.rgb
                    ?: palette.darkMutedSwatch?.rgb

                val dark = palette.darkMutedSwatch?.rgb
                    ?: palette.darkVibrantSwatch?.rgb

                val primaryColor = vibrant?.let { Color(it) } ?: NeoViolet
                val secondaryColor = secondary?.let { Color(it) } ?: NeoCyan
                val backdropColor = dark?.let { Color(it).copy(alpha = 0.85f) } ?: DeepSpaceCharcoal

                return@withContext DynamicPaletteColors(
                    primaryAccent = primaryColor,
                    secondaryAccent = secondaryColor,
                    darkBackdrop = backdropColor
                )
            }
        } catch (_: Exception) {
            // Safe fallback
        }

        DynamicPaletteColors()
    }
}
