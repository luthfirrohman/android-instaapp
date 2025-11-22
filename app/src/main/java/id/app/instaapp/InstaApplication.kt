package id.app.instaapp

import android.app.Application
import com.google.firebase.FirebaseApp

class InstaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
