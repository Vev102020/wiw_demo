package core.errors

import com.wiwchat.shared.common.data.errors.MediaErrorCode
import io.ktor.http.HttpStatusCode

sealed class MediaException(
    code: MediaErrorCode,
    message: String,
    httpStatus: HttpStatusCode
) : AppException(code, message, httpStatus)

class FileNotFoundException : MediaException(
    code = MediaErrorCode.MEDIA_FILE_NOT_FOUND,
    message = "Файл не найден в запросе",
    httpStatus = HttpStatusCode.BadRequest
)

class UploadFailedException(cause: String) : MediaException(
    code = MediaErrorCode.MEDIA_UPLOAD_FAILED,
    message = "Ошибка сохранения в хранилище: $cause",
    httpStatus = HttpStatusCode.InternalServerError
)