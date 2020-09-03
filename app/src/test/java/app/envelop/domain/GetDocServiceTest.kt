package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.data.models.Upload
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.UploadRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test


class GetDocServiceTest {

    val docRepositoryMock = mock<DocRepository>()
    val uploadRepositoryMock = mock<UploadRepository>()

    val docService = GetDocService(docRepositoryMock, uploadRepositoryMock)

    @Test
    fun get() {
        val value = Observable.just(Optional.create("Hello World"))
        whenever(docRepositoryMock.get(any())).then {
            value
        }

        val result = docService.get("")

        assertEquals(
            result,
            value
        )
    }

    @Test
    fun getUpload() {
        val uploadObject = Upload()
        val value = Flowable.just(listOf(uploadObject))
        whenever(uploadRepositoryMock.getByDocId(any())).then {
            value
        }

        val result = docService.getUpload("")
                                .blockingFirst()
                                .element()
        assertEquals(
            uploadObject,
            result
        )
    }
}
