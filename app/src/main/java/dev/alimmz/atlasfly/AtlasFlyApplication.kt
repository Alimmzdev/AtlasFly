package dev.alimmz.atlasfly

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import dev.alimmz.atlasfly.core.network.AtlasFlyHttpClient
import io.ktor.client.HttpClient
import javax.inject.Inject

@HiltAndroidApp
class AtlasFlyApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    @AtlasFlyHttpClient
    lateinit var httpClient: HttpClient

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                @OptIn(ExperimentalCoilApi::class)
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .build()
    }
}
