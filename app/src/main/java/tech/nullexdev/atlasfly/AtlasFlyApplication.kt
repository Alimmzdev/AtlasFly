package tech.nullexdev.atlasfly

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class AtlasFlyApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader {
        val httpClient = HttpClient(OkHttp)

        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                @OptIn(ExperimentalCoilApi::class)
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .build()
    }
}
