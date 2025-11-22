package id.app.instaapp.data.model

enum class MediaType {
    IMAGE,
    VIDEO;

    companion object {
        fun fromValue(value: String?): MediaType {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: IMAGE
        }
    }
}