package profile.model

class ProfileValidationException(val field: String? = null) : Exception("Invalid profile API input")

class RemoteHttpException(
    val status: Int,
    val responseBody: String?,
    val retryAfterSeconds: Long?,
) : Exception("Profile API request failed with HTTP $status")
