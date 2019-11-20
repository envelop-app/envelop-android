package app.envelop.ui.donate

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.R
import app.envelop.test.AppHelper
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DonateActivityTest {

  @get:Rule
  val intentTestRule = IntentsTestRule<DonateActivity>(DonateActivity::class.java)
  private val context = AppHelper.context

  @Test
  fun donateCryptoTest() {
    val cryptoUri = Uri.parse(context.getString(R.string.donate_crypto_link))
    onView(withId(R.id.donateCrypto)).perform(click())
    intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(cryptoUri)))
  }

  @Test
  fun donateStacksTest() {
    val stacksUri = Uri.parse(context.getString(R.string.donate_stack_link))
    onView(withId(R.id.donateStacks)).perform(click())
    intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(stacksUri)))
  }

  @Test
  fun donatePaypalTest() {
    val stacksUri = Uri.parse(context.getString(R.string.donate_paypal_link))
    onView(withId(R.id.donatePaypal)).perform(click())
    intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(stacksUri)))
  }
}
