package di


import core.plugins.*
import features.auth.domain.service.*
import features.profile.domain.service.UserService
import org.koin.dsl.module

val serverBackendModule = module {
    single { EmailService(config = get()) }

    single { AuthService(
            jwtAlgorithm = jwtAlgorithm,
            jwtIssuer = jwtIssuer,
            jwtAudience = jwtAudience,
            emailService = get()
        )}
    single { UserService() }
}