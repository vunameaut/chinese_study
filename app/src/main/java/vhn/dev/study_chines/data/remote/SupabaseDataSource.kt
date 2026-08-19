package vhn.dev.study_chines.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import vhn.dev.study_chines.data.remote.SupabaseClientProvider.client

class SupabaseDataSource {

    // === Sessions ===
    fun fetchSessionsFlow(): Flow<List<SessionDto>> = flow {
        try {
            val sessions = client.postgrest.from("sessions").select().decodeList<SessionDto>()
            emit(sessions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun createSession(title: String): Long? = withContext(Dispatchers.IO) {
        try {
            val dto = SessionDto(title = title)
            val result = client.postgrest.from("sessions").insert(dto).decodeSingle<SessionDto>()
            result.id.toLong()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteSession(sessionId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary").delete { eq("session_id", sessionId) }
            client.postgrest.from("sessions").delete { eq("id", sessionId) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMasteredCount(sessionId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId); eq("review_status", 2) }
                .decodeList<VocabularyDto>()
            vocabs.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTotalCount(sessionId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
            vocabs.size
        } catch (e: Exception) {
            0
        }
    }

    // === Vocabulary ===
    fun getVocabularyBySessionFlow(sessionId: Int): Flow<List<VocabularyDto>> = flow {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
            emit(vocabs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getVocabularyForReviewFlow(sessionId: Int): Flow<List<VocabularyDto>> = flow {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
            emit(vocabs.filter { it.reviewStatus != 2 })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun insertVocabulary(vocab: VocabularyDto): Long? = withContext(Dispatchers.IO) {
        try {
            val result = client.postgrest.from("vocabulary").insert(vocab).decodeSingle<VocabularyDto>()
            result.id.toLong()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVocabulary(vocab: VocabularyDto): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary").update(vocab) { eq("id", vocab.id) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRandomPinyinDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> = withContext(Dispatchers.IO) {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
            vocabs.filter { it.id != excludeId }.map { it.pinyin }.shuffled().take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRandomMeaningDistractors(excludeId: Int, sessionId: Int, limit: Int = 3): List<String> = withContext(Dispatchers.IO) {
        try {
            val vocabs = client.postgrest.from("vocabulary")
                .select { eq("session_id", sessionId) }
                .decodeList<VocabularyDto>()
            vocabs.filter { it.id != excludeId }.map { it.meaning }.shuffled().take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
