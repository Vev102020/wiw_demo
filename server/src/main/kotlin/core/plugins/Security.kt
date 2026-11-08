package core.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

internal lateinit var jwtSecret: String
internal lateinit var jwtIssuer: String
internal lateinit var jwtAudience: String
internal lateinit var jwtAlgorithm: Algorithm

fun Application.configureSecurity() {

    // Читаем конфиг один раз при старте
    jwtSecret = environment.config.property("jwt.secret").getString()
    jwtIssuer = environment.config.property("jwt.issuer").getString()
    jwtAudience = environment.config.property("jwt.audience").getString()
    jwtAlgorithm = Algorithm.HMAC256(jwtSecret)
    val appRealName = environment.config.property("app.name").getString()

    authentication {
        jwt("auth-jwt") {
            realm = appRealName

            // Передаем верификатор напрямую, используя уже созданный jwtAlgorithm
            verifier(
                JWT.require(jwtAlgorithm)
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

