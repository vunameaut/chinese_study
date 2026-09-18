package vhn.dev.study_chines.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import vhn.dev.study_chines.data.model.ClassifierPoint

class ClassifierSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testClassifierDeserialization() {
        val rawJson = """
            {
              "id": 23,
              "session_id": 1,
              "hsk_level": 1,
              "lesson_num": 1,
              "lesson_title": "HSK 1 Bài 1 - 你好 / Chào anh",
              "hanzi": "位",
              "pinyin": "wèi",
              "meaning": "vị, ngài (chỉ người lịch sự)",
              "usage_note": "Dùng cho người khi cần thể hiện sự tôn trọng, lịch sự và trang nhã (thầy cô, khách quý, đồng nghiệp).",
              "collocations": [
                {
                  "noun": "老师",
                  "phrase": "一位老师",
                  "pinyin": "lǎoshī",
                  "meaning": "một vị thầy cô giáo"
                }
              ],
              "examples": [
                {
                  "hanzi": "您好，王老师是一位非常好的老师。",
                  "pinyin": "Nín hǎo, Wáng lǎoshī shì yí wèi fēicháng hǎo de lǎoshī.",
                  "meaning": "Chào thầy, thầy Vương là một người thầy rất tốt."
                }
              ],
              "exercises": [
                {
                  "type": "choice",
                  "answer": "位",
                  "explain": "Chỉ thầy cô (老师) trang trọng lịch sự dùng lượng từ '位'.",
                  "options": ["位", "张", "条", "本"],
                  "question": "校门口站着一___老师。"
                }
              ],
              "order_index": 1,
              "created_at": "2026-09-16T05:24:39.040583+00:00"
            }
        """.trimIndent()

        val item = json.decodeFromString<ClassifierPoint>(rawJson)
        println("Decoded classifier: '${item.classifier}'")
        println("Decoded explanation: '${item.exercises.firstOrNull()?.explanation}'")
        
        // Let's see what it currently is:
        assertEquals("位", item.classifier)
        assertEquals("Chỉ thầy cô (老师) trang trọng lịch sự dùng lượng từ '位'.", item.exercises.first().explanation)
    }

    @Test
    fun testLiveSupabaseFetchClassifiers() = kotlinx.coroutines.runBlocking {
        val ds = vhn.dev.study_chines.data.remote.SupabaseDataSource()
        val lessons = ds.getAllClassifierLessons(1)
        println("Fetched lessons count for HSK 1: ${lessons.size}")
        lessons.forEach {
            println("Lesson ${it.lessonNum}: ${it.title}, classifiers: ${it.classifiers}, collocations: ${it.previewCollocations}")
        }
        org.junit.Assert.assertTrue("HSK 1 should have classifier lessons", lessons.isNotEmpty())
        val firstLesson = lessons.first()
        assertEquals(1, firstLesson.lessonNum)
        org.junit.Assert.assertTrue("First lesson should have classifiers", firstLesson.classifiers.isNotEmpty())
        org.junit.Assert.assertTrue("First lesson should contain '位'", firstLesson.classifiers.first().contains("位"))

        val classifiers = ds.getClassifiers(1, 1)
        println("Classifiers for lesson 1: ${classifiers.size}")
        org.junit.Assert.assertTrue("Classifiers list should not be empty", classifiers.isNotEmpty())
        val c1 = classifiers.first()
        assertEquals("位", c1.classifier)
        assertEquals("wèi", c1.pinyin)
        org.junit.Assert.assertTrue("Exercises should not be empty", c1.exercises.isNotEmpty())
        assertEquals("位", c1.exercises.first().answer)
        org.junit.Assert.assertTrue("Explanation should not be empty", c1.exercises.first().explanation.isNotBlank())
    }
}
