package vhn.dev.study_chines.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import vhn.dev.study_chines.BuildConfig

object SupabaseClientProvider {

    private val supabaseUrl: String = BuildConfig.SUPABASE_URL.ifBlank {
        "https://qthuomifuzyrzhgcsedx.supabase.co"
    }

    private val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY.ifBlank {
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF0aHVvbWlmdXp5cnpoZ2NzZWR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODcxNjEyNTUsImV4cCI6MjEwMjczNzI1NX0.47yUgABy8oaXYsMNYCC5UPqBkQVORJvf5VDa6wnBlwo"
    }

    val client: SupabaseClient by lazy {
        createClient()
    }

    private fun createClient(): SupabaseClient {
        require(supabaseAnonKey.isNotBlank()) {
            "Supabase anon key is missing. Add supabase.anon.key to local.properties or gradle.properties."
        }

        return createSupabaseClient(supabaseUrl, supabaseAnonKey) {
            install(Postgrest)
        }
    }

    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            defaultRequest {
                headers.append("apikey", supabaseAnonKey)
                headers.append("Authorization", "Bearer $supabaseAnonKey")
            }
        }
    }
}
