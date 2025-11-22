package id.app.instaapp.data.model

data class NewPostPayload(
    val caption: String,
    val mediaBytes: ByteArray?,
    val mediaType: MediaType,
    val fileExtension: String,
    val accessLevel: AccessLevel,
    val allowedUserIds: List<String> = emptyList()
)