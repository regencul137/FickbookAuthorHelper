package com.example.fickbookauthorhelper.logic

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface IEventProvider {
    val events: SharedFlow<IEvent>
}

interface IEventEmitter {
    fun pushEvent(event: IEvent)
}

interface IEvent

sealed class BaseEvent : IEvent {
    data object ConnectionCorrupted : BaseEvent()
}

class EventBus @Inject constructor() : IEventProvider, IEventEmitter {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _events = MutableSharedFlow<IEvent>()
    override val events: SharedFlow<IEvent> = _events.asSharedFlow()

    override fun pushEvent(event: IEvent) {
        println("EventBus >> $event")
        scope.launch { _events.emit(event) }
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