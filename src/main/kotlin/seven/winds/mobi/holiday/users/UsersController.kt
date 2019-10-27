package seven.winds.mobi.holiday.users

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import java.util.stream.Collectors
import java.util.Random
import seven.winds.mobi.holiday.secret.Secret
import seven.winds.mobi.holiday.secret.SecretsRepository
import seven.winds.mobi.holiday.tokens.Token
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import seven.winds.mobi.holiday.objects.Authorization
import seven.winds.mobi.holiday.objects.ChangePassword

@RestController
@RequestMapping("/users")
class UsersController (
        internal val usersRepository: UsersRepository,
        internal val tokensRepository: TokensRepository,
        internal val secretsRepository: SecretsRepository
) {

    //@Autowired lateinit var sender: JavaMailSender

    @PostMapping("/registration")
    fun registrationUser(@RequestBody newUser: User, request: HttpServletRequest): ResponseEntity<Any> {
        if(usersRepository.existsByLogin(newUser.login)) return ResponseEntity(HttpStatus.CONFLICT)

        val secret = secretsRepository.findOneByValue(request.getHeader("Secret"))
        if (secret != null) {
            secretsRepository.delete(secret)

            val encoder = BCryptPasswordEncoder()
            newUser.password = encoder.encode(newUser.password)
            newUser.registrationTime = System.currentTimeMillis()
            usersRepository.save(newUser)
            val tokenValue = encoder.encode("holiday_user_" + newUser.id + "_" + request.getHeader("User-Agent"))
            tokensRepository.save(Token(
                    tokenValue,
                    newUser.id!!,
                    System.currentTimeMillis(),
                    TokensFunction.STATUS_USER
            ))
            return ResponseEntity("""{"token":"$tokenValue"}""", HttpStatus.CREATED)
        } else return ResponseEntity(HttpStatus.FORBIDDEN)
    }

    @PostMapping("/login")
    fun loginUser(@RequestBody authorization: Authorization, request: HttpServletRequest): ResponseEntity<Any> {
        val user = usersRepository.findOneByLogin(authorization.login!!)
        val encoder = BCryptPasswordEncoder()
        return when {
            user == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            encoder.matches(authorization.password, user.password) -> {
                user.lastActiveTime = System.currentTimeMillis()
                usersRepository.save(user)
                val tokenValue = encoder.encode("holiday_user_" + user.id + "_" + request.getHeader("User-Agent"))
                val token = tokensRepository.findOneByValue(tokenValue)
                if(token != null) {
                    token.time = System.currentTimeMillis()
                    tokensRepository.save(token)
                } else {
                    tokensRepository.save(Token(
                            tokenValue,
                            user.id!!,
                            System.currentTimeMillis(),
                            TokensFunction.STATUS_PARTNER
                    ))
                }
                ResponseEntity(mapOf("user" to user, "token" to tokenValue), HttpStatus.OK)
            }
            else -> ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @PutMapping("/update")
    fun updateUser(@RequestBody newUser: User, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return when (val user = if(token != null) usersRepository.getOne(token.userId) else null) {
            null -> ResponseEntity(HttpStatus.NOT_FOUND)
            else -> {
                if(user.email != newUser.email && secretsRepository.findOneByValue(request.getHeader("Secret")) == null) {
                    ResponseEntity(HttpStatus.FORBIDDEN)
                } else {
                    secretsRepository.deleteByValue(request.getHeader("Secret"))

                    user.name = newUser.name
                    user.email = newUser.email
                    user.phone = newUser.phone
                    usersRepository.save(user)
                    ResponseEntity(HttpStatus.OK)
                }
            }
        }
    }

    @PutMapping("/password")
    fun changeUserPassword(@RequestBody changePassword: ChangePassword, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        val encoder = BCryptPasswordEncoder()
        val user = if(token != null) usersRepository.getOne(token.userId) else null
        return when {
            user == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            encoder.matches(changePassword.oldPassword, user.password) -> {
                user.password = encoder.encode(changePassword.newPassword)
                usersRepository.save(user)
                ResponseEntity(HttpStatus.OK)
            }
            else -> ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @GetMapping("/check-email")
    fun getEmailCode(request: HttpServletRequest): ResponseEntity<Any> {
        val secret = Secret(
                System.currentTimeMillis(),
                System.currentTimeMillis().toString().substring(7)
        )

        /*val message = sender.createMimeMessage()
        val helper = MimeMessageHelper(message)

        helper.setTo(request.getHeader("Email"))
        helper.setText("Ваш код подтверждения Email для holiday.com:\n\n${secret.value}\n\nЕсли код запрашивали не Вы, то никому не сообщайте его.")
        helper.setSubject("Код подтверждения для holiday.com")

        sender.send(message)*/

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity("""
    {"address":"${request.getHeader("Email")}",
    "subject":"Код подтверждения для holiday.com",
    "text":"Ваш код подтверждения Email для holiday.com:\n\n${secret.value}\n\nЕсли код запрашивали не Вы, то никому не сообщайте его.",
    "from":"support@holiday.com"}
    """.trimIndent(), headers)
        val response = RestTemplate().postForObject("https://admire.social/back/send-mail.php", entity, String::class.java)

        return if(ObjectMapper().readTree(response).get("result").asBoolean()) {
            secretsRepository.save(secret)
            ResponseEntity(response, HttpStatus.OK)
        } else {
            ResponseEntity(response, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(request: HttpServletRequest): ResponseEntity<Any> {
        val user = usersRepository.findOneByLogin(request.getHeader("Email"))
        if(user != null) {
            val password = Random()
                    .ints(10, 33, 122)
                    .mapToObj { i -> i.toChar().toString() }
                    .collect(Collectors.joining())
            user.password = password

            /*val message = sender.createMimeMessage()
            val helper = MimeMessageHelper(message)

            helper.setTo(user.email)
            helper.setText("Вы воспользовались сбросом пароля. Это ваш новый пароль, воспользуйтесь им для входа в мобильное приложение:\n\n$password")
            helper.setSubject("Сброс пароля для holiday.com")

            sender.send(message)*/
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity("""
    {"address":"${user.email}",
    "subject":"Сброс пароля для holiday.com",
    "text":"Вы воспользовались сбросом пароля. Это ваш новый пароль, воспользуйтесь им для входа в мобильное приложение:\n\n$password",
    "from":"support@holiday.com"}
    """.trimIndent(), headers)
            RestTemplate().postForObject("https://admire.social/back/send-mail.php", entity, String::class.java)
        }
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/check")
    fun checkUser(request: HttpServletRequest): ResponseEntity<Any> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token")) != null) {
            ResponseEntity("""{"is":true}""", HttpStatus.OK)
        } else {
            ResponseEntity("""{"is":false}""", HttpStatus.OK)
        }
    }
}