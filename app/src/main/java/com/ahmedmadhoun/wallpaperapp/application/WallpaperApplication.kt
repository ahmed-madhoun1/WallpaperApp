package com.ahmedmadhoun.wallpaperapp.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WallpaperApplication : Application(){

    companion object{
        const val AD_MOB_ID = "ca-app-pub-6699628738766818~6308110057"
    }

}
