package app.envelop.domain

import app.envelop.common.FileHandler
import app.envelop.common.Operation
import app.envelop.common.Optional
import app.envelop.data.models.Upload
import app.envelop.data.models.UploadState
import app.envelop.data.repositories.DocRepository
import app.envelop.data.repositories.RemoteRepository
import app.envelop.data.repositories.UploadRepository
import app.envelop.data.security.Encrypter
import app.envelop.data.security.EncrypterProvider
import app.envelop.data.security.EncryptionKey
import app.envelop.data.security.KeyGenerator
import app.envelop.test.DocFactory
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadServiceTest {

  private val remoteRepositoryMock = mock<RemoteRepository>()
  private val docRepositoryMock = mock<DocRepository>()
  private val uploadRepositoryMock = mock<UploadRepository>()
  private val fileHandlerMock = mock<FileHandler>()
  private val updateDocRemotelyMock = mock<UpdateDocRemotely>()
  private val encrypterProviderMock = mock<EncrypterProvider>()
  private val keyGeneratorMock = mock<KeyGenerator>()

  private val uploadService = UploadService(
    remoteRepositoryMock,
    docRepositoryMock,
    uploadRepositoryMock,
    fileHandlerMock,
    updateDocRemotelyMock,
    encrypterProviderMock,
    keyGeneratorMock
  )

  @Test
  fun emptyUpload() {
    whenever(uploadRepositoryMock.getAll()).thenReturn(Flowable.just(listOf()))

    val stateStream = uploadService.state().test()
    uploadService.startUpload()

    stateStream.assertValue(UploadState.Idle)
  }

  @Test
  fun startUpload() {
    val uploadFile = Upload(partSize = 1_000)
    val fileDocument = DocFactory.fivePartsBuild()

    whenever(uploadRepositoryMock.getAll()).thenReturn(Flowable.just(listOf(uploadFile)))
    whenever(docRepositoryMock.get(any())).thenReturn(Observable.just(Optional.create(fileDocument)))
    whenever(keyGeneratorMock.generate(any(), any())).thenReturn(EncryptionKey(key = ByteArray(0)))
    whenever(fileHandlerMock.localFileToByteArray(any(), any(), any())).thenReturn(ByteArray(0))
    whenever(uploadRepositoryMock.get(any())).thenReturn(Flowable.just(listOf(uploadFile)))

    val stateStream = uploadService.state().test()
    uploadService.startUpload()

    val uploadValue = stateStream.values()[1]

    assertThat(uploadValue, instanceOf(UploadState.Uploading::class.java))
    val unwrappedUpload = uploadValue as UploadState.Uploading

    assertEquals(unwrappedUpload.nextUpload.doc, fileDocument)
    assertEquals(unwrappedUpload.nextUpload.upload, uploadFile)
  }

  @Test
  fun stopUpload() {
    val uploadFile = Upload(partSize = 1_000)
    val fileDocument = DocFactory.fivePartsBuild()
    val encryptedMock = mock<Encrypter>()

    whenever(uploadRepositoryMock.getAll()).thenReturn(Flowable.just(listOf(uploadFile)))
    whenever(docRepositoryMock.get(any())).thenReturn(Observable.just(Optional.create(fileDocument)))
    whenever(keyGeneratorMock.generate(any(), any())).thenReturn(EncryptionKey(key = ByteArray(0)))
    whenever(fileHandlerMock.localFileToByteArray(any(), any(), any())).thenReturn(ByteArray(0))
    whenever(encrypterProviderMock.getOrError(any())).thenReturn(encryptedMock)
    whenever(encryptedMock.encrypt(any(), any(), any())).thenReturn(Encrypter.Result(ByteArray(1)))
    whenever(remoteRepositoryMock.uploadByteArray(any(), any(), any())).thenReturn(Single.never<Operation<Unit>>())

    val stateStream = uploadService.state().test()
    uploadService.startUpload()

    val uploadValue = stateStream.values().last()
    assertThat(uploadValue, instanceOf(UploadState.Uploading::class.java))

    uploadService.stopUpload()

    val stopDownload = stateStream.values().last()
    assertThat(stopDownload, instanceOf(UploadState.Idle::class.java))
  }
}
