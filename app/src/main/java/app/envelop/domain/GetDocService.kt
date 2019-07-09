package app.envelop.domain

import app.envelop.common.Optional
import app.envelop.data.repositories.DocRepository
import javax.inject.Inject

class GetDocService
@Inject constructor(
  private val docRepository: DocRepository
) {

  fun get(id: String) =
    docRepository
      .get(id)
      .map { Optional.create(it.firstOrNull()) }
      .toObservable()
}