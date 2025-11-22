package id.app.instaapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import id.app.instaapp.ui.InstaViewModelFactory
import id.app.instaapp.ui.auth.AuthViewModel
import id.app.instaapp.ui.feed.FeedViewModel
import id.app.instaapp.ui.postcreate.CreatePostViewModel


@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: InstaViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    abstract fun bindFeedViewModel(viewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePostViewModel::class)
    abstract fun bindCreatePostViewModel(viewModel: CreatePostViewModel): ViewModel
}