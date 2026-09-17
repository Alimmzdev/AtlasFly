package profile.model

sealed interface PatchField<out T> {
    data object Omitted : PatchField<Nothing>
    data class Supplied<T>(val value: T) : PatchField<T>
}
