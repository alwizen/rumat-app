package com.solu8i.cmms

import com.solu8i.cmms.data.remote.RetrofitInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitUrlTest {

    @Test
    fun emptyUrlIsRejected() {
        assertFalse(RetrofitInstance.isAllowedUrl(""))
        assertFalse(RetrofitInstance.isAllowedUrl("   "))
    }

    @Test
    fun httpsUrlIsAlwaysAllowed() {
        assertTrue(RetrofitInstance.isAllowedUrl("https://rumat.ft-tegal.com/api/"))
    }

    @Test
    fun cleartextIsGovernedByBuildFlag() {
        val httpUrl = "http://10.0.2.2/api/"
        val result = RetrofitInstance.isAllowedUrl(httpUrl)

        if (BuildConfig.ALLOW_CLEARTEXT) {
            assertTrue(result)
        } else {
            assertFalse(result)
        }
    }

    @Test
    fun baseUrlWithoutApiPathGetsApiAppended() {
        assertEquals(
            "http://192.168.110.112:8000/api/",
            RetrofitInstance.normalizeBaseUrl("http://192.168.110.112:8000")
        )
        assertEquals(
            "http://192.168.110.112:8000/api/",
            RetrofitInstance.normalizeBaseUrl("http://192.168.110.112:8000/")
        )
    }

    @Test
    fun baseUrlWithApiPathIsPreserved() {
        assertEquals(
            "https://rumat.ft-tegal.com/api/",
            RetrofitInstance.normalizeBaseUrl("https://rumat.ft-tegal.com/api/")
        )
        assertEquals(
            "https://rumat.ft-tegal.com/api/",
            RetrofitInstance.normalizeBaseUrl("https://rumat.ft-tegal.com/api")
        )
    }

    @Test
    fun deeperPathIsPreservedWithTrailingSlash() {
        assertEquals(
            "https://example.com/v1/api/",
            RetrofitInstance.normalizeBaseUrl("https://example.com/v1/api")
        )
    }
}

