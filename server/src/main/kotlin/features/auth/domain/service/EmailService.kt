package features.auth.domain.service

import io.ktor.server.config.ApplicationConfig
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class EmailService(config: ApplicationConfig) {
    private val appName = config.property("app.name").getString()
    // Mail.ru SMTP
    private val smtpHost = config.property("smtp.host").getString()
    private val smtpPort = config.property("smtp.port").getString()
    private val senderEmail = config.property("smtp.email").getString()
    private val senderPassword = config.property("smtp.password").getString()

    private fun sendEmail(to: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
            put("mail.smtp.socketFactory.port", smtpPort)
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
//            put("mail.smtp.socketFactory.fallback", "false")
            put("mail.smtp.ssl.enable", "true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(senderEmail, "WIW Chat", "UTF-8"))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject
            setText(body, "UTF-8")
        }

        Transport.send(message)
    }

    // Новый пользователь
    fun sendRegistrationOtp(email: String, code: String) {
        sendEmail(
            to = email,
            subject = "Код подтверждения регистрации — $appName",
            body = """
                Здравствуйте!
                
                Ваш код подтверждения регистрации: $code
                
                Код действителен в течение 5 минут.
                Если вы не запрашивали регистрацию в приложении $appName, просто проигнорируйте данное письмо.
            """.trimIndent()
        )
    }
    // Существующий
    fun sendAccountAlreadyExistsAlert(email: String) {
        sendEmail(
            to = email,
            subject = "Попытка регистрации аккаунта — $appName",
            body = """
                Здравствуйте!
                
                Кто-то (возможно, вы) попытался создать новый аккаунт в $appName, используя этот адрес электронной почты.
                
                Однако к данной почте уже привязан существующий аккаунт. 
                
                * Если это были вы: пожалуйста, откройте приложение и выполните Вход, используя ваши учетные данные. Если вы забыли пароль, вы можете воспользоваться функцией восстановления доступа на экране входа.
                * Если это были не вы: кто-то ошибочно ввел ваш Email. Никаких действий предпринимать не нужно, ваш текущий аккаунт находится в полной безопасности.
            """.trimIndent()
        )
    }

    fun sendResetPasswordCode(email: String, code: String) {
        sendEmail(
            to = email,
            subject = "Восстановление доступа — $appName",
            body = """
            Здравствуйте!
            
            Вы запросили восстановление пароля для вашего аккаунта в $appName.
            
            Ваш проверочный код: $code
            
            Код действителен в течение 5 минут.
            Если вы не запрашивали сброс пароля, просто проигнорируйте данное письмо — ваш текущий пароль останется в безопасности.
        """.trimIndent()
        )
    }

    fun sendWelcomeEmail(email: String) {
        sendEmail(
            to = email,
            subject = "Добро пожаловать в $appName!",
            body = """
            Здравствуйте!
            
            Этот адрес электронной почты был успешно привязан к новому аккаунту в $$appName. 
                       
            С уважением,
            Команда поддержки $$appName
        """.trimIndent()
        )
    }

    fun sendPasswordChangedNotification(email: String) {
        sendEmail(
            to = email,
            subject = "Изменение пароля на $appName!",
            body = """
            Здравствуйте!
            
                Пароль от вашего аккаунта $$appName был успешно изменен.
                
                Если вы совершали данное действие, просто проигнорируйте это письмо.
                Если вы НЕ меняли пароль, ваш аккаунт мог быть взломан. В таком случае немедленно обратитесь в службу поддержки безопасности для блокировки учетной записи.
                       
            С уважением,
            Команда поддержки $$appName
        """.trimIndent()
        )
    }
}