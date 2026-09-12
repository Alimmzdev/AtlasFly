package profile.model

sealed interface ApiResult<out T> {
    data object Loading : ApiResult<Nothing>
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

sealed interface ApiError {
    data object MissingAuthentication : ApiError
    data object Unauthorized : ApiError
    data object Forbidden : ApiError
    data class Validation(val field: String? = null) : ApiError
    data object NotFound : ApiError
    data class RateLimited(val retryAfterSeconds: Long? = null) : ApiError
    data object Server : ApiError
    data object Network : ApiError
    data object Serialization : ApiError
    data object Configuration : ApiError
    data class Unknown(val cause: Throwable? = null) : ApiError
}
