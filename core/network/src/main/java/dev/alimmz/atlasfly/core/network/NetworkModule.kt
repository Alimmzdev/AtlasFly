package dev.alimmz.atlasfly.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton
import io.ktor.http.HttpHeaders

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = false
        explicitNulls = true
        encodeDefaults = false
    }

    @Provides
    @SupabasePublishableKey
    fun provideSupabasePublishableKey(): String = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    @Provides
    @Singleton
    @AtlasFlyHttpClient
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        engine {
            preconfigured = OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .build()
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("HttpClient", message)
                }
            }
            level = LogLevel.HEADERS
            sanitizeHeader { header ->
                header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                    header.equals(API_KEY_HEADER, ignoreCase = true)
            }
        }

        expectSuccess = false
    }

    private const val API_KEY_HEADER = "apikey"
}
