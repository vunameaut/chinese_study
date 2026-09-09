package vhn.dev.study_chines.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    // Helper logic độc lập để test hàm isNewerVersion
    private fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        try {
            val rClean = remoteVersion.removePrefix("v").trim()
            val lClean = localVersion.removePrefix("v").trim()
            val remoteParts = rClean.split(".").mapNotNull { it.toIntOrNull() }
            val localParts = lClean.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
        } catch (_: Exception) {
            return remoteVersion != localVersion
        }
        return false
    }

    @Test
    fun testIsNewerVersion_newerMinor_returnsTrue() {
        assertTrue(isNewerVersion("2.1", "2.0"))
        assertTrue(isNewerVersion("v2.1", "2.0"))
        assertTrue(isNewerVersion("v2.1.0", "v2.0"))
    }

    @Test
    fun testIsNewerVersion_newerPatch_returnsTrue() {
        assertTrue(isNewerVersion("2.0.1", "2.0"))
        assertTrue(isNewerVersion("v2.0.1", "v2.0.0"))
    }

    @Test
    fun testIsNewerVersion_newerMajor_returnsTrue() {
        assertTrue(isNewerVersion("3.0", "2.9.9"))
        assertTrue(isNewerVersion("10.0", "2.0"))
    }

    @Test
    fun testIsNewerVersion_sameVersion_returnsFalse() {
        assertFalse(isNewerVersion("2.0", "2.0"))
        assertFalse(isNewerVersion("v2.0", "2.0"))
        assertFalse(isNewerVersion("2.0.0", "2.0"))
    }

    @Test
    fun testIsNewerVersion_olderVersion_returnsFalse() {
        assertFalse(isNewerVersion("1.9", "2.0"))
        assertFalse(isNewerVersion("v1.5.2", "v2.0"))
    }
}
