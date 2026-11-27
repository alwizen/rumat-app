package com.solu8i.cmms

import com.solu8i.cmms.data.remote.MediaUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlTest {

    private val storageBase = "https://rumat.ft-tegal.com/storage/"

    @Test
    fun blankPathReturnsNull() {
        assertNull(MediaUrl.resolve(null, storageBase))
        assertNull(MediaUrl.resolve("", storageBase))
        assertNull(MediaUrl.resolve("   ", storageBase))
    }

    @Test
    fun absoluteUrlIsPassedThrough() {
        val presigned = "https://bucket.r2.cloudflarestorage.com/rumat-media/a.webp?X-Amz-Signature=abc"

        assertEquals(presigned, MediaUrl.resolve(presigned, storageBase))
        assertEquals(
            "https://example.com/a.webp",
            MediaUrl.resolve("https://example.com/a.webp", storageBase)
        )
    }

    @Test
    fun localUrisArePassedThrough() {
        assertEquals(
            "content://media/external/images/1",
            MediaUrl.resolve("content://media/external/images/1", storageBase)
        )
        assertEquals(
            "file:///data/cache/photo_1.jpg",
            MediaUrl.resolve("file:///data/cache/photo_1.jpg", storageBase)
        )
    }

    @Test
    fun relativePathIsJoinedToStorageBase() {
        assertEquals(
            "https://rumat.ft-tegal.com/storage/maintenance-requests/a.webp",
            MediaUrl.resolve("maintenance-requests/a.webp", storageBase)
        )
    }

    @Test
    fun leadingSlashAndTrailingBaseSlashAreNormalized() {
        assertEquals(
            "https://rumat.ft-tegal.com/storage/housekeeping/requests/a.webp",
            MediaUrl.resolve("/housekeeping/requests/a.webp", "https://rumat.ft-tegal.com/storage/")
        )
    }

    @Test
    fun surroundingWhitespaceIsTrimmed() {
        assertEquals(
            "https://rumat.ft-tegal.com/storage/a.webp",
            MediaUrl.resolve("  a.webp  ", storageBase)
        )
    }

    @Test
    fun isAbsoluteDetectsSupportedSchemes() {
        assertTrue(MediaUrl.isAbsolute("https://x/y"))
        assertTrue(MediaUrl.isAbsolute("HTTP://x/y"))
        assertTrue(MediaUrl.isAbsolute("content://x/y"))
        assertTrue(MediaUrl.isAbsolute("file:///x"))
        assertFalse(MediaUrl.isAbsolute("maintenance-requests/a.webp"))
    }
}
