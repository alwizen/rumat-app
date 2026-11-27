package com.solu8i.cmms.data.remote

/**
 * Resolusi URL media foto agar app tetap jalan baik saat media disimpan
 * di disk lokal maupun di Cloudflare R2 (bucket privat).
 *
 * Backend dapat mengirim salah satu bentuk berikut:
 * - URL absolut (mis. presigned URL R2) -> dipakai apa adanya.
 * - Path relatif (disk lokal, mis. `maintenance-requests/x.webp`) -> digabung
 *   ke base URL storage server.
 * - URI lokal (`content://`, `file://`) untuk pratinjau galeri/kamera.
 */
object MediaUrl {

    fun resolve(path: String?, storageBaseUrl: String): String? {
        val raw = path?.trim().orEmpty()

        if (raw.isEmpty()) {
            return null
        }

        if (isAbsolute(raw)) {
            return raw
        }

        val base = storageBaseUrl.trimEnd('/')

        return "$base/${raw.trimStart('/')}"
    }

    fun isAbsolute(value: String): Boolean {
        val trimmed = value.trim()

        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true) ||
            trimmed.startsWith("android.resource://", ignoreCase = true)
    }
}
