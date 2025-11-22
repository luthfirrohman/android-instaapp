package id.app.instaapp.data.model

enum class AccessLevel {
    PUBLIC,
    FOLLOWERS,
    PRIVATE;

    companion object {
        fun fromValue(value: String?): AccessLevel {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PUBLIC
        }
    }
}