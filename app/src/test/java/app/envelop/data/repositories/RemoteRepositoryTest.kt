package app.envelop.data.repositories

import app.envelop.common.Operation
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.blockstack.android.sdk.BlockstackSession
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Provider

class RemoteRepositoryTest {

    @Test
    fun exceptionHandling() {
      val blockStackSessionMock = mock<BlockstackSession>()
      val exception = RuntimeException()

      GlobalScope.launch {
        whenever(blockStackSessionMock.getFile(any(), any())).thenThrow(exception)
      }

      val remoteRepo = RemoteRepository(Provider { blockStackSessionMock }, Gson())
      val result = remoteRepo.getJson("index", Unit::class, true).blockingGet()

      assertEquals(
        Operation.error<RemoteRepository>(exception),
        result
      )
    }
}
