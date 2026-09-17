package dev.alimmz.atlasfly.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        check(BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()) {
            "SUPABASE_PUBLISHABLE_KEY is missing"
        }
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth) {
                scheme = AUTH_SCHEME
                host = AUTH_HOST
                flowType = FlowType.PKCE
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthTokenProvider(
        provider: SupabaseAuthTokenProvider,
    ): AuthTokenProvider = provider

    private const val SUPABASE_URL = "https://tstjkjlbdxnsqhjtzvau.supabase.co"
    private const val AUTH_SCHEME = "atlasfly"
    private const val AUTH_HOST = "auth"
}
