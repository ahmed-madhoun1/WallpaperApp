package com.ahmedmadhoun.wallpaperapp.di

import android.app.Activity
import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Job

@InstallIn(ActivityComponent::class)
@Module
object ActivityModule {

    @Provides
    @ActivityScoped
    fun provideCoroutineJob(): Job = Job()


}