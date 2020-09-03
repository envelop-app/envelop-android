package app.envelop.common.rx


import app.envelop.common.Operation
import app.envelop.common.Optional
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Exception
import java.net.SocketTimeoutException

class RxSingleToOperationKtTest {

  @Test
  fun rxSingleOperationSuccess() {
    val stream = rxSingleToOperation {
      Optional.None
    }

    val result = stream.blockingGet()

    assertEquals(
      Operation.success(Optional.None),
      result
    )
  }

  @Test
  fun rxSingleOperationSuccessValue() {
    val stream = rxSingleToOperation {
      Optional.create(true)
    }

    val result = stream.blockingGet()

    assertEquals(
      Operation.success(Optional.Some(true)),
      result
    )
  }

  @Test
  fun rxSingleOperationErrorExceptionSubscribed() {

    val exception = SocketTimeoutException()
    val stream = rxSingleToOperation {
      throw exception
    }

    val result = stream.blockingGet()

    assertEquals(
      Operation.error<Exception>(exception),
      result
    )
  }



}
