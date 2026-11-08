package core.util

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.util.UUID

object S3Manager {
    private val bucketName = System.getenv("S3_BUCKET_NAME") ?: "wiw-chat-media"

    private val s3Client: S3Client = S3Client.builder()
        // Подключаемся к контейнеру Silo внутри сети Docker
        .endpointOverride(URI.create(System.getenv("S3_ENDPOINT") ?: "http://localhost:9000"))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    System.getenv("S3_ACCESS_KEY") ?: "wiw_s3_root",
                    System.getenv("S3_SECRET_KEY") ?: "complex_password"
                )
            )
        )
        .region(Region.US_EAST_1) // Для локального Silo регион может быть любым
        .build()


    // Загружает файл в Silo и возвращает уникальное имя файла
    fun uploadFile(fileBytes: ByteArray, originalName: String, contentType: String): String {
        // Генерируем случайное имя UUID, чтобы пользователи не перезаписывали файлы друг друга
        val extension = originalName.substringAfterLast('.', "")
        val secureFileName = "${UUID.randomUUID()}.$extension"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(secureFileName)
            .contentType(contentType)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes))

        return secureFileName
    }
}