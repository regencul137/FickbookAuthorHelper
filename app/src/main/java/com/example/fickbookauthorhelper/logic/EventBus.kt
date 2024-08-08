package com.example.fickbookauthorhelper.logic

import android.graphics.drawable.Drawable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

interface IEventProvider {
    val events: SharedFlow<FHEvent>
}

interface IEventEmitter {
    suspend fun emit(event: FHEvent)
}

sealed class FHEvent {
    data object SignedInEvent : FHEvent()
    data object SignedOutEvent : FHEvent()
    data object CheckSignInSuccess : FHEvent()
    data class UserNameLoaded(val name: String) : FHEvent()
    data class UserAvatarLoaded(val avatar: Drawable?) : FHEvent()
}

class EventBus @Inject constructor() : IEventProvider, IEventEmitter {
    private val _events = MutableSharedFlow<FHEvent>()
    override val events: SharedFlow<FHEvent> = _events.asSharedFlow()

    override suspend fun emit(event: FHEvent) {
        _events.emit(event)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object EventBusModule {
    @Singleton
    @Provides
    fun provideEventBus(): EventBus {
        return EventBus()
    }

    @Singleton
    @Provides
    fun provideEventProvider(eventBus: EventBus): IEventProvider = eventBus

    @Singleton
    @Provides
    fun provideEventEmitter(eventBus: EventBus): IEventEmitter = eventBus
}