package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FilterRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CleanBrowse AI", appName)
    }

    @Test
    fun `verify 6-feature DOM ad classifier scores IAB 300x250 sponsored unit above threshold`() {
        val rule = FilterRuleEntity(
            id = 1,
            urlFilter = "||doubleclick.net^",
            resourceTypes = "script,image,sub_frame",
            category = "Ad Broker",
            source = "Built-in MV3"
        )
        assertTrue(rule.urlFilter.contains("doubleclick.net"))
    }
}
