package com.example.fickbookauthorhelper.logic.di

import com.example.fickbookauthorhelper.logic.IEventEmitter
import com.example.fickbookauthorhelper.logic.http.HttpConnectionChecker
import com.example.fickbookauthorhelper.logic.http.HttpFeedLoader
import com.example.fickbookauthorhelper.logic.http.HttpSignInChecker
import com.example.fickbookauthorhelper.logic.http.HttpSignInHelper
import com.example.fickbookauthorhelper.logic.http.HttpSignOutHelper
import com.example.fickbookauthorhelper.logic.http.HttpUserLoadHelper
import com.example.fickbookauthorhelper.logic.http.IHttpConnectionChecker
import com.example.fickbookauthorhelper.logic.http.IHttpFeedLoader
import com.example.fickbookauthorhelper.logic.http.IHttpSignInChecker
import com.example.fickbookauthorhelper.logic.http.IHttpSignInHelper
import com.example.fickbookauthorhelper.logic.http.IHttpSignOutHelper
import com.example.fickbookauthorhelper.logic.http.IHttpUserLoadHelperUser
import com.example.fickbookauthorhelper.logic.http.client.IClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpModule {
    @Singleton
    @Provides
    fun provideHttpSignInChecker(
        clientProvider: IClientProvider,
        eventEmitter: IEventEmitter
    ): HttpSignInChecker {
        return HttpSignInChecker(clientProvider, eventEmitter)
    }

    @Singleton
    @Provides
    fun provideConnectionChecker(
        eventEmitter: IEventEmitter
    ): HttpConnectionChecker {
        return HttpConnectionChecker(eventEmitter)
    }

    @Singleton
    @Provides
    fun provideHttpFeedLoader(
        clientProvider: IClientProvider,
        eventEmitter: IEventEmitter
    ): HttpFeedLoader {
        return HttpFeedLoader(clientProvider, eventEmitter)
    }

    @Singleton
    @Provides
    fun provideUserLoadHelper(clientProvider: IClientProvider): HttpUserLoadHelper {
        return HttpUserLoadHelper(clientProvider)
    }

    //region Interfaces

    @Singleton
    @Provides
    fun provideISignInChecker(signInChecker: HttpSignInChecker): IHttpSignInChecker = signInChecker

    @Singleton
    @Provides
    fun provideISignInHelper(signInHelper: HttpSignInHelper): IHttpSignInHelper = signInHelper

    @Singleton
    @Provides
    fun provideISignOutHelper(signOutHelper: HttpSignOutHelper): IHttpSignOutHelper = signOutHelper

    @Singleton
    @Provides
    fun provideIConnectionChecker(connectionChecker: HttpConnectionChecker): IHttpConnectionChecker = connectionChecker

    @Singleton
    @Provides
    fun provideIFeedLoader(feedLoader: HttpFeedLoader): IHttpFeedLoader = feedLoader

    @Singleton
    @Provides
    fun provideIUserLoadHelper(userLoadHelper: HttpUserLoadHelper): IHttpUserLoadHelperUser = userLoadHelper

    // endregion
}