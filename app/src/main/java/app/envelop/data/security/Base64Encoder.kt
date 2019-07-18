package app.envelop.data.security

import android.util.Base64
import javax.inject.Inject

open class Base64Encoder
@Inject constructor() {

  open fun encode(input: ByteArray) =
    Base64.encodeToString(input, Base64.NO_WRAP)

  open fun decode(input: String) =
    Base64.decode(input, Base64.NO_WRAP)

}

