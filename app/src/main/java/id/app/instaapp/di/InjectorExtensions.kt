package id.app.instaapp.di

import android.content.Context
import id.app.instaapp.InstaApplication

val Context.appComponent: AppComponent
    get() = (applicationContext as InstaApplication).appComponent