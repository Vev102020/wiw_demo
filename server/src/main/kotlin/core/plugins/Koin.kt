package core.plugins

import com.wiwchat.shared.common.di.sharedModules
import core.util.CryptoUtils
import core.util.RedisManager
import core.util.S3Manager
import di.infrastructureModule
import di.serverBackendModule
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()

        val ktorConfigModule = module {
            single<ApplicationConfig> { environment.config }
        }

        modules(ktorConfigModule + infrastructureModule + sharedModules + serverBackendModule)
    }
}

