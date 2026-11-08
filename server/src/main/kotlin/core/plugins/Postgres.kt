package core.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.flywaydb.core.Flyway

fun Application.configurePostgres() {
    // Читаем конфиг
    val dbUrl = environment.config.property("postgres.url").getString()
    val dbUser = environment.config.property("postgres.user").getString()
    val dbPassword = environment.config.property("postgres.password").getString()

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(false)
        //.baselineVersion("3") // Все что до этой версии уже старое
//        .cleanDisabled(false) // 1. Разрешаем очистку (по умолчанию она может быть заблокирована)
        .load()
        .apply {
//            clean() // 2. ОЧИЩАЕМ ВСЮ БАЗУ И ИСТОРИЮ НАМЕРТВО
            migrate() // 3. Запускаем накат чистых исправленных скриптов с V1 по V6
        }

    Database.connect(dataSource)


    log.info("✅ PostgreSQL подключен")
}
