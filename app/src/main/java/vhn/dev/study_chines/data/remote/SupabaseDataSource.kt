package vhn.dev.study_chines.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vhn.dev.study_chines.data.remote.SupabaseClientProvider.client
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.data.remote.VocabularyDto

class SupabaseDataSource {

    suspend fun fetchSessions(): List<SessionDto> = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("sessions").select().decodeList<SessionDto>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createSession(title: String): SessionDto? = withContext(Dispatchers.IO) {
        try {
            val dto = SessionDto(title = title)
            client.postgrest.from("sessions").insert(dto).decodeSingle<SessionDto>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchVocabulariesBySession(sessionId: Int): List<VocabularyDto> = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertVocabulary(vocab: VocabularyDto): VocabularyDto? = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary").insert(vocab).decodeSingle<VocabularyDto>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVocabularyStatus(id: Int, status: Int, lastReviewedAt: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary").update(
                mapOf("review_status" to status, "last_reviewed_at" to lastReviewedAt)
            ) { eq("id", id) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteSession(sessionId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete vocabularies first, then session
            client.postgrest.from("vocabulary").delete { eq("session_id", sessionId) }
            client.postgrest.from("sessions").delete { eq("id", sessionId) }
            true
        } catch (e: Exception) {
            false
        }
    }
}
