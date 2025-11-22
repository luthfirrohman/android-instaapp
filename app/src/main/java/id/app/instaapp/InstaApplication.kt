package id.app.instaapp

import android.app.Application
import com.google.firebase.FirebaseApp
import id.app.instaapp.di.AppComponent
import id.app.instaapp.di.DaggerAppComponent

class InstaApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
