package com.vk.starscriptmusic.domain.model

/**
 * Domain representation of an Artist/Band for auto-generated hubs.
 */
data class Artist(
    val name: String,
    val trackCount: Int,
    val albumCount: Int = 1,
    val representativeArtUri: String? = null,
    val accentColorArgb: Int? = null
)
