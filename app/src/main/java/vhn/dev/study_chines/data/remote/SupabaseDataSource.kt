package vhn.dev.study_chines.data.remote

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import vhn.dev.study_chines.data.remote.SupabaseClientProvider.client

private const val TAG = "SupabaseDataSource"

class SupabaseDataSource {

    // === Sessions ===
    fun fetchSessionsFlow(): Flow<List<SessionDto>> = flow {
        val sessions = client.postgrest.from("sessions").select().decodeList<SessionDto>()
        emit(sessions)
    }.catch { e ->
        Log.e(TAG, "Error fetching sessions", e)
        emit(emptyList())
    }

    suspend fun createSession(title: String): Long? = withContext(Dispatchers.IO) {
        try {
            val dto = SessionDto(title = title, createdAt = java.time.Instant.now().toString())
            Log.d(TAG, "Creating session: $dto")
            val result = client.postgrest.from("sessions").insert(dto).decodeSingle<SessionDto>()
            Log.d(TAG, "Session created with ID: ${result.id}")
            result.id.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session: $title", e)
            null
        }
    }

    suspend fun deleteSession(sessionId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("vocabulary").delete { eq("session_id", sessionId) }
            client.postgrest.from("sessions").delete { eq("id", sessionId) }
            Log.d(TAG, "Session deleted: $sessionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session: $sessionId", e)
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
            Log.e(TAG, "Error getting mastered count for session: $sessionId", e)
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
            Log.e(TAG, "Error getting total count for session: $sessionId", e)
            0
        }
    }

    // === Vocabulary ===
    fun getVocabularyBySessionFlow(sessionId: Int): Flow<List<VocabularyDto>> = flow {
        val vocabs = client.postgrest.from("vocabulary")
            .select { eq("session_id", sessionId) }
            .decodeList<VocabularyDto>()
        emit(vocabs)
    }.catch { e ->
        Log.e(TAG, "Error fetching vocabulary for session: $sessionId", e)
        emit(emptyList())
    }

    fun getVocabularyForReviewFlow(sessionId: Int): Flow<List<VocabularyDto>> = flow {
        val vocabs = client.postgrest.from("vocabulary")
            .select { eq("session_id", sessionId) }
            .decodeList<VocabularyDto>()
        emit(vocabs.filter { it.reviewStatus != 2 })
    }.catch { e ->
        Log.e(TAG, "Error fetching vocabulary for review in session: $sessionId", e)
        emit(emptyList())
    }

    suspend fun insertVocabulary(vocab: VocabularyDto): Long? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inserting vocabulary: hanzi=${vocab.hanzi}, sessionId=${vocab.sessionId}")
            val result = client.postgrest.from("vocabulary").insert(vocab).decodeSingle<VocabularyDto>()
            Log.d(TAG, "Vocabulary inserted with ID: ${result.id}")
            result.id.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting vocabulary: ${vocab.hanzi}", e)
            null
        }
    }

    suspend fun updateVocabulary(vocab: VocabularyDto): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating vocabulary: id=${vocab.id}, status=${vocab.reviewStatus}")
            client.postgrest.from("vocabulary").update(vocab) { eq("id", vocab.id) }
            Log.d(TAG, "Vocabulary updated: ${vocab.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vocabulary: ${vocab.id}", e)
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
            Log.e(TAG, "Error getting pinyin distractors for session: $sessionId", e)
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
            Log.e(TAG, "Error getting meaning distractors for session: $sessionId", e)
            emptyList()
        }
    }
}
