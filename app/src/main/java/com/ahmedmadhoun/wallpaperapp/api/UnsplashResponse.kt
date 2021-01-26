package com.ahmedmadhoun.wallpaperapp.api

import com.ahmedmadhoun.wallpaperapp.model.UnsplashPhoto

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)