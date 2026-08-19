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

object SupabaseClientProvider {

    // === BẠN CẬP NHẬT 3 GIÁ TRỊ NÀY ===
    private const val SUPABASE_URL = "https://qthuomifuzyrzhgcsedx.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbG...Blwo"
    // ==================================

    val client: SupabaseClient by lazy {
        createClient()
    }

    private fun createClient(): SupabaseClient {
        return createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Postgrest)
        }
    }

    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            defaultRequest {
                headers.append("apikey", SUPABASE_ANON_KEY)
                headers.append("Authorization", "Bearer $SUPABASE_ANON_KEY")
            }
        }
    }
}