package id.app.instaapp.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import id.app.instaapp.ui.InstaViewModelFactory
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class,
        RepositoryModule::class,
        ViewModelModule::class,
        DispatcherModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    fun viewModelFactory(): InstaViewModelFactory
}