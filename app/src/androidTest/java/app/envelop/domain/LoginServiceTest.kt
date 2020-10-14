package app.envelop.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.envelop.common.Operation
import app.envelop.data.BlockstackLogin
import app.envelop.data.repositories.UserRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.blockstack.android.sdk.model.UserData
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginServiceTest {

    val blockstackLoginMock = mock<BlockstackLogin>()
    val userRepositoryMock = mock<UserRepository>()

    val finishLoginService = LoginService(blockstackLoginMock, userRepositoryMock)


    @Test
    fun login() {
        val result = finishLoginService.login()
        verify(blockstackLoginMock).login()
    }

    @Test
    fun finishLogin() {
        whenever(blockstackLoginMock.handlePendingSignIn(any())).thenReturn(
            Single.just(Operation(UserData(JSONObject().put("username", "JohnDoe.blockstack.id"))))
        )

        val result = finishLoginService.finishLogin("/token1/token2/token3/token4").blockingGet()
        assert(result.isSuccessful)
    }

    @Test
    fun finishLoginUnverifiedUsername() {
        whenever(blockstackLoginMock.handlePendingSignIn(any())).thenReturn(
            Single.just(Operation(UserData(JSONObject().put("username", "null"))))
        )

        val result = finishLoginService.finishLogin("/token1/token2/token3/token4").blockingGet()
        assert(result.isError)
    }

    @Test
    fun finishLoginNoUsername() {
        whenever(blockstackLoginMock.handlePendingSignIn(any())).thenReturn(
            Single.just(Operation(UserData(JSONObject())))
        )

        val result = finishLoginService.finishLogin("/token1/token2/token3/token4").blockingGet()
        assert(result.isError)
    }

}
