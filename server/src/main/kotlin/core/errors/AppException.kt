package core.errors

import com.wiwchat.shared.common.data.errors.ApiErrorCode
import io.ktor.http.HttpStatusCode

open class AppException(
    val code: ApiErrorCode,
    override val message: String,
    val httpStatus: HttpStatusCode
) : Exception(message)