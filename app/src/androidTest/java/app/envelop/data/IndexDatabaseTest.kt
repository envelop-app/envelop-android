package app.envelop.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.data.models.Doc
import app.envelop.data.models.Index
import app.envelop.test.AppHelper.appComponent
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IndexDatabaseTest {

  private val doc = Doc(
    id = "ABCDEF",
    name = "file.pdf",
    url = "UUID-UUID",
    size = 1_000,
    contentType = null,
    numParts = 1,
    encryptionSpec = null,
    username = "username"
  )

  private lateinit var db: IndexDatabase

  @Before
  fun setUp() {
    db = appComponent.indexDatabase()
  }

  @After
  fun tearDown() {
    db.delete()
  }

  @Test
  fun getReactively() {
    val test = db.get().test()
    repeat(3) {
      db.save(Index()).blockingAwait()
    }
    test.awaitCount(4)
  }

  @Test
  fun saveAndGet() {
    db.save(Index(listOf(doc))).blockingAwait()
    db.get().test().awaitCount(1).assertValue {
      it.docs.size == 1
    }
  }
}