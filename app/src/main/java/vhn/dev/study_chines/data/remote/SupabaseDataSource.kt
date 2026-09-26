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

    suspend fun createSession(title: String, hskLevel: Int = 1): Long? = withContext(Dispatchers.IO) {
        try {
            val dto = SessionDto(title = title, hskLevel = hskLevel, createdAt = java.time.Instant.now().toString())
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
        val review = vocabs.filter { it.reviewStatus != 2 }
        emit(if (review.isEmpty()) vocabs else review)
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

            // Lấy từ cần học để biết số âm tiết cần match
            val targetVocab = vocabs.find { it.id == excludeId }
            val targetSyllableCount = if (targetVocab != null) getSyllableCount(targetVocab) else 1

            val seen = mutableSetOf(targetVocab?.pinyin?.trim()?.lowercase() ?: "")
            val candidates = mutableListOf<String>()

            // Ưu tiên: distractor cùng số âm tiết
            vocabs.filter { it.id != excludeId }.shuffled().forEach { v ->
                if (candidates.size >= limit) return@forEach
                val p = v.pinyin.trim()
                if (getSyllableCount(v) == targetSyllableCount && !seen.contains(p.lowercase())) {
                    seen.add(p.lowercase())
                    candidates.add(p)
                }
            }

            // Fallback: nếu không đủ, lấy bất kỳ
            if (candidates.size < limit) {
                vocabs.filter { it.id != excludeId }.shuffled().forEach { v ->
                    if (candidates.size >= limit) return@forEach
                    val p = v.pinyin.trim()
                    if (!seen.contains(p.lowercase())) {
                        seen.add(p.lowercase())
                        candidates.add(p)
                    }
                }
            }

            candidates
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pinyin distractors for session: $sessionId", e)
            emptyList()
        }
    }

    /**
     * Đếm số âm tiết của một từ vựng.
     * Ưu tiên đếm số chữ Hán trong hanzi; nếu không có thì đếm số phần trong pinyin.
     */
    private fun getSyllableCount(vocab: VocabularyDto): Int {
        // Ưu tiên: đếm chữ Hán (mỗi chữ = 1 âm tiết)
        val cleanHanzi = vocab.hanzi.replace(Regex("[^\\u4e00-\\u9fa5]"), "")
        if (cleanHanzi.isNotEmpty()) return cleanHanzi.length
        // Fallback: đếm số phần pinyin (cách nhau bởi khoảng trắng hoặc dấu gạch ngang)
        if (vocab.pinyin.isNotBlank()) {
            return vocab.pinyin.trim().split(Regex("[\\s\\-]+")).size
        }
        return 1
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

    // === Classifiers (Lượng từ) ===
    suspend fun getClassifiers(hskLevel: Int, lessonNum: Int, sessionId: Long? = null): List<vhn.dev.study_chines.data.model.ClassifierPoint> = withContext(Dispatchers.IO) {
        try {
            if (sessionId != null && sessionId > 0) {
                val custom = client.postgrest.from("classifiers")
                    .select { eq("session_id", sessionId) }
                    .decodeList<vhn.dev.study_chines.data.model.ClassifierPoint>()
                if (custom.isNotEmpty()) return@withContext custom.sortedBy { it.orderIndex }
            }
            val list = client.postgrest.from("classifiers")
                .select {
                    eq("hsk_level", hskLevel)
                    eq("lesson_num", lessonNum)
                }
                .decodeList<vhn.dev.study_chines.data.model.ClassifierPoint>()
            list.sortedBy { it.orderIndex }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching classifiers: sessionId=$sessionId, hsk=$hskLevel, lesson=$lessonNum", e)
            emptyList()
        }
    }

    suspend fun getAllClassifierLessons(hskLevel: Int): List<vhn.dev.study_chines.data.model.ClassifierLessonItem> = withContext(Dispatchers.IO) {
        try {
            val list = client.postgrest.from("classifiers")
                .select {
                    eq("hsk_level", hskLevel)
                }
                .decodeList<vhn.dev.study_chines.data.model.ClassifierPoint>()
            val grouped = list.groupBy { it.lessonNum }
            grouped.keys.sorted().map { lessonNum ->
                val items = grouped[lessonNum] ?: emptyList()
                val title = items.firstOrNull()?.lessonTitle ?: "Bài $lessonNum"
                val classifiers = items.map { "${it.classifier} ${it.pinyin}".trim() }.filter { it.isNotBlank() }
                val collocations = items.flatMap { it.collocations.map { c -> c.phrase.ifEmpty { "${it.classifier} ${c.noun}".trim() } } }.take(4)
                vhn.dev.study_chines.data.model.ClassifierLessonItem(
                    hskLevel = hskLevel,
                    lessonNum = lessonNum,
                    title = title,
                    classifiers = classifiers,
                    previewCollocations = collocations
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching classifier lessons for hsk: $hskLevel", e)
            emptyList()
        }
    }

    // === Grammar ===
    suspend fun getGrammarPoints(sessionId: Long?, hskLevel: Int, lessonNum: Int): List<vhn.dev.study_chines.data.model.GrammarPoint> = withContext(Dispatchers.IO) {
        try {
            if (sessionId != null && sessionId > 0) {
                val custom = client.postgrest.from("grammar")
                    .select { eq("session_id", sessionId) }
                    .decodeList<vhn.dev.study_chines.data.model.GrammarPoint>()
                if (custom.isNotEmpty()) return@withContext custom.sortedBy { it.orderIndex }
            }
            val list = client.postgrest.from("grammar")
                .select {
                    eq("hsk_level", hskLevel)
                    eq("lesson_num", lessonNum)
                }
                .decodeList<vhn.dev.study_chines.data.model.GrammarPoint>()
            list.sortedBy { it.orderIndex }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching grammar points: hsk=$hskLevel, lesson=$lessonNum", e)
            emptyList()
        }
    }

    suspend fun getAllGrammarLessons(hskLevel: Int): List<vhn.dev.study_chines.data.model.LessonItem> = withContext(Dispatchers.IO) {
        try {
            val list = client.postgrest.from("grammar")
                .select {
                    eq("hsk_level", hskLevel)
                }
                .decodeList<vhn.dev.study_chines.data.model.GrammarPoint>()
            val sorted = list.sortedBy { it.lessonNum }
            val map = linkedMapOf<Int, String>()
            sorted.forEach { p ->
                if (!map.containsKey(p.lessonNum)) {
                    map[p.lessonNum] = p.lessonTitle ?: "Bài ${p.lessonNum}"
                }
            }
            map.map { vhn.dev.study_chines.data.model.LessonItem(hskLevel, it.key, it.value) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching grammar lessons", e)
            emptyList()
        }
    }
}
