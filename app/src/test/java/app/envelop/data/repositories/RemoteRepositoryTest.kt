package app.envelop.data.repositories

import app.envelop.common.Operation
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.blockstack.android.sdk.BlockstackSession
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import javax.inject.Provider

class RemoteRepositoryTest {

  @Test
  fun exceptionHandling() = runBlocking {
    val blockStackSessionMock = mock<BlockstackSession>()

    GlobalScope.launch {
      whenever(blockStackSessionMock.getFile(any(), any())).thenThrow(RuntimeException())
    }

      val remoteRepo = RemoteRepository(Provider { blockStackSessionMock }, Gson())
      val result = remoteRepo.getJson("index", Unit::class, true).blockingGet()


    assertThat(result, instanceOf(Operation.error<Unit>(RuntimeException())::class.java))

  }
}

