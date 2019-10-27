package seven.winds.mobi.holiday.administrator

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import seven.winds.mobi.holiday.actions.*
import seven.winds.mobi.holiday.images.FileSystemStorageService
import seven.winds.mobi.holiday.objects.ChangePassword
import seven.winds.mobi.holiday.partners.Partner
import seven.winds.mobi.holiday.partners.PartnersRepository
import seven.winds.mobi.holiday.secret.Secret
import seven.winds.mobi.holiday.secret.SecretsRepository
import seven.winds.mobi.holiday.tokens.Token
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import seven.winds.mobi.holiday.users.User
import seven.winds.mobi.holiday.users.UsersRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/administrator")
class AdministratorController (
        internal val actionsDemoRepository: ActionsDemoRepository,
        internal val actionsDetailRepository: ActionsDetailRepository,
        internal val updatedActionRepository: UpdatedActionRepository,
        internal val partnersRepository: PartnersRepository,
        internal val usersRepository: UsersRepository,
        private val administratorsRepository: AdministratorsRepository,
        internal val tokensRepository: TokensRepository,
        internal val secretsRepository: SecretsRepository
) {

    val storageService = FileSystemStorageService

    init {
        if(administratorsRepository.findAll().isEmpty()) {
            administratorsRepository.save(Administrator(
                    "admin",
                    BCryptPasswordEncoder().encode("admin")
            ))

            /*val restTemplate = RestTemplate().getForObject("https://admire.social/back/cities-ru.json", String::class.java)
            if(restTemplate != null) {
                ObjectMapper().readTree(restTemplate).forEach {
                    citiesRepository.save(City(it.get("city").asText(), it.get("latitude").asDouble(), it.get("longitude").asDouble()))
                }
            }*/
        }
    }

    @GetMapping("/secrets")
    fun getSecrets(request: HttpServletRequest): ResponseEntity<List<Secret>> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token"))?.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(secretsRepository.findAll(), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @PostMapping("/login")
    fun loginAdministrator(request: HttpServletRequest): ResponseEntity<Any> {
        val administrator = administratorsRepository.findAll()[0]
        val encoder = BCryptPasswordEncoder()
        return if(administrator.login == request.getHeader("login") &&
                encoder.matches(request.getHeader("password"), administrator.password)
        ) {
            val tokenValue = encoder.encode("holiday_administrator_" + request.getHeader("User-Agent"))
            val token = tokensRepository.findOneByValue(tokenValue)
            if(token != null) {
                token.time = System.currentTimeMillis()
                tokensRepository.save(token)
            } else {
                tokensRepository.save(Token(
                        tokenValue,
                        administrator.id,
                        System.currentTimeMillis(),
                        TokensFunction.STATUS_ADMINISTRATOR
                ))
            }

            ResponseEntity.ok().body("""{"token":"$tokenValue"}""")
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/password")
    fun changeAdministratorPassword(@RequestBody changePassword: ChangePassword, request: HttpServletRequest): ResponseEntity<Any> {
        val encoder = BCryptPasswordEncoder()
        val administrator = administratorsRepository.findAll()[0]
        return if(administrator.login == changePassword.oldLogin &&
                encoder.matches(changePassword.oldPassword, administrator.password)) {
            administrator.login = changePassword.newLogin!!
            administrator.password = changePassword.newPassword
            administratorsRepository.save(administrator)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.CONFLICT)
    }

    @GetMapping("/get-actions")
    fun getActions(): ResponseEntity<MutableList<NewAction>> {
        return ResponseEntity(updatedActionRepository.findAll(), HttpStatus.OK)
    }

    @PostMapping("/add-action")
    fun addAction(@RequestBody newAction: NewAction, request: HttpServletRequest): ResponseEntity<Any> {
        val token = request.getHeader("Token")
        return if(token != null) {
            newAction.createTime = System.currentTimeMillis()
            val restEntity =
                    RestTemplate().getForEntity("https://geocoder.api.here.com/6.2/geocode.json?searchtext=${newAction.address}&app_id=UdRH6PlISTlADYsW6mzl&app_code=lfrrTheP9nBedeJyy1NtIA&gen=8", String::class.java)
            val mapper = ObjectMapper()
            val response = restEntity.body
            val root = mapper.readTree(response)
            val location = try {
                root.get("Response").get("View").get(0).get("Result").get(0).get("Location").get("DisplayPosition")
            } catch (e: Exception) {
                null
            }

            if(newAction.originId != 0L) {
                clearById(newAction.originId, newAction.image)
            }
            if(newAction.id != null && newAction.id > 0L) {
                clearById(newAction.id, newAction.image)
            }

            storageService.replaceFile(newAction.image, storageService.TYPE_ACTION_IMAGE)

            val actionDemo = ActionDemo(
                    newAction.title,
                    newAction.profit,
                    newAction.oldCost,
                    newAction.newCost,
                    newAction.category,
                    newAction.subCategory,
                    newAction.image,
                    newAction.partnerId,
                    location?.get("Latitude")?.asDouble(),
                    location?.get("Longitude")?.asDouble(),
                    newAction.isInteresting,
                    newAction.isCategoryTop,
                    newAction.actionBefore
            )
            actionsDemoRepository.save(actionDemo)

            val actionDetail = ActionDetail(
                    actionDemo.id!!,
                    newAction.address,
                    newAction.phone,
                    newAction.workTime,
                    newAction.site,
                    newAction.information,
                    newAction.socialNetworks,
                    newAction.createTime
            )
            actionsDetailRepository.save(actionDetail)
            ResponseEntity(HttpStatus.CREATED)
        } else {
            ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @GetMapping("/get/partners/{page}")
    fun getPartners(@PathVariable("page") page: Int, request: HttpServletRequest): ResponseEntity<MutableList<Partner>> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token"))?.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(partnersRepository.findFirst10By(PageRequest.of(page, 10)), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping("/get/users/{page}")
    fun getUsers(@PathVariable("page") page: Int, request: HttpServletRequest): ResponseEntity<MutableList<User>> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token"))?.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(usersRepository.findFirst10By(PageRequest.of(page, 10)), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping("/check")
    fun checkAdministrator(request: HttpServletRequest): ResponseEntity<Any> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token")) != null) {
            ResponseEntity("""{"is":true}""", HttpStatus.OK)
        } else {
            ResponseEntity("""{"is":false}""", HttpStatus.OK)
        }
    }

    @PutMapping("/update/partner/{id}")
    fun updatePartner(@PathVariable("id") id: Long, @RequestBody newPartner: Partner, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token?.status == TokensFunction.STATUS_ADMINISTRATOR) {
            val partner = partnersRepository.getOne(id)
            partner.firstName = newPartner.firstName
            partner.lastName = newPartner.lastName
            partner.middleName = newPartner.middleName
            partner.email = newPartner.email
            partnersRepository.save(partner)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)
    }

    @PutMapping("/update/user/{id}")
    fun updateUser(@PathVariable("id") id: Long, @RequestBody newUser: User, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token?.status == TokensFunction.STATUS_ADMINISTRATOR) {
            val user = usersRepository.getOne(id)
            user.login = newUser.login
            user.name = newUser.name
            user.email = newUser.email
            user.phone = newUser.phone
            usersRepository.save(user)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)
    }

    fun clearById(id: Long, image: String) {
        if(actionsDemoRepository.existsById(id)) {
            val actionDemo = actionsDemoRepository.getOne(id)
            if(actionDemo.image != image) storageService.deleteFile(actionDemo.image)
            actionsDemoRepository.deleteById(id)
            actionsDetailRepository.deleteById(actionsDetailRepository.findByActionId(id).id!!)
        } else if(updatedActionRepository.existsById(id)) {
            val updatedAction = updatedActionRepository.getOne(id)
            if(updatedAction.image != image) storageService.deleteFile(updatedAction.image)
            updatedActionRepository.deleteById(id)

            if(updatedAction.originId != 0L) {
                clearById(updatedAction.originId, image)
            }
        }
    }
}