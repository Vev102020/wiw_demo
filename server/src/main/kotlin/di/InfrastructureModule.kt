package di

import core.util.CryptoUtils
import core.util.RedisManager
import core.util.S3Manager
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

val infrastructureModule = module {
    single(createdAtStart = true) {
        val config = get<ApplicationConfig>()

        val cryptoSecret = config.property("crypto.secret").getString()
        CryptoUtils.init(cryptoSecret)
        RedisManager.init(config)
        S3Manager
    }
}