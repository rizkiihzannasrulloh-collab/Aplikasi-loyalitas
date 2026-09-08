package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Customer
import com.example.util.QrCodeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nasi Cokot", appName)
  }

  @Test
  fun `test customer loyalty 8 stamps rule`() {
    val customerWith7 = Customer(
      id = "NC-123456",
      name = "Testing Customer",
      phone = "08123456789",
      currentStamps = 7
    )
    assertFalse(customerWith7.isRewardReady)
    assertEquals(1, customerWith7.stampsRemaining)

    val customerWith8 = customerWith7.copy(currentStamps = 8)
    assertTrue(customerWith8.isRewardReady)
    assertEquals(0, customerWith8.stampsRemaining)
  }

  @Test
  fun `test qr code generator generates valid bitmap`() {
    val bitmap = QrCodeHelper.generateQrBitmap("NC-998877", size = 200)
    assertNotNull(bitmap)
    assertEquals(200, bitmap?.width)
    assertEquals(200, bitmap?.height)
  }
}

