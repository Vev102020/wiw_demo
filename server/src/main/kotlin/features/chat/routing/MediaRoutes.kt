package features.chat.routing

import com.wiwchat.shared.features.chat.data.dto.UploadMediaResponse
import core.errors.FileNotFoundException
import core.errors.UploadFailedException
import core.util.S3Manager
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray

fun Route.mediaRouting() {
    route("/chat/upload") {
        post {
            val multipart = call.receiveMultipart()
            var fileBytes: ByteArray? = null
            var originalName = ""
            var contentType = ""

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    originalName = part.originalFileName ?: "file.bin"
                    contentType = part.contentType?.toString() ?: "application/octet-stream"
                    fileBytes = part.provider().readBuffer().readByteArray()
                }
                part.release()
            }

            val bytes = fileBytes ?: throw FileNotFoundException()


            try {
                val secureFileName = S3Manager.uploadFile(bytes, originalName, contentType)
                // Формируем ссылку на ваше локальное хранилище Silo
                val fileUrl = "http://localhost:9000/wiw-chat-media/$secureFileName"

                // database.saveMessage(...)

                // Идеальный, безопасный и типизированный ответ:
                val response = UploadMediaResponse(
                    url = fileUrl,
                    fileName = secureFileName,
                    contentType = contentType
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                // Маскируем системную ошибку в наше понятное исключение
                throw UploadFailedException(e.localizedMessage ?: "Неизвестная ошибка S3")
            }
        }
    }
}