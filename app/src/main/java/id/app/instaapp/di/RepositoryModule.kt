package id.app.instaapp.di

import dagger.Binds
import dagger.Module
import id.app.instaapp.data.repository.AuthRepository
import id.app.instaapp.data.repository.AuthRepositoryImpl
import id.app.instaapp.data.repository.PostRepository
import id.app.instaapp.data.repository.PostRepositoryImpl
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}