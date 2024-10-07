package com.example.fickbookauthorhelper.logic.feed

sealed class FeedType {
    data object VIEWS : FeedType()
    data object KUDOS : FeedType()
    data object REVIEWS : FeedType()
    data object SUBSCRIPTIONS : FeedType()
    data object WAITS : FeedType()

    companion object {
        fun fromString(type: String): FeedType {
            return when (type.uppercase()) {
                "VIEWS" -> VIEWS
                "KUDOS" -> KUDOS
                "REVIEWS" -> REVIEWS
                "SUBSCRIPTIONS" -> SUBSCRIPTIONS
                "WAITS" -> WAITS
                else -> throw IllegalArgumentException("Unknown FeedType: $type")
            }
        }

        fun toString(type: FeedType): String {
            return when (type) {
                VIEWS -> "VIEWS"
                KUDOS -> "KUDOS"
                REVIEWS -> "REVIEWS"
                SUBSCRIPTIONS -> "SUBSCRIPTIONS"
                WAITS -> "WAITS"
            }
        }
    }
}