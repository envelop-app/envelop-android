package app.envelop.domain

import app.envelop.common.EnvelopSpec
import app.envelop.data.repositories.DocRepository
import app.envelop.data.security.HashGenerator
import javax.inject.Inject

class DocIdGenerator
@Inject constructor(
  private val hashGenerator: HashGenerator,
  private val docRepository: DocRepository
) {

  fun generate(): String {
    lateinit var docId: String
    do {
      docId = hashGenerator.generate(EnvelopSpec.POINTER_LENGTH)
    } while (existingIds.contains(docId))
    return docId
  }

  private val existingIds by lazy {
    docRepository.list().blockingFirst().map { it.id }
  }

}

