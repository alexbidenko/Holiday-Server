package seven.winds.mobi.holiday.callback

import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import seven.winds.mobi.holiday.blocklist.BlockedPartnersRepository
import seven.winds.mobi.holiday.blocklist.BlockedUsersRepository
import seven.winds.mobi.holiday.partners.Partner
import seven.winds.mobi.holiday.partners.PartnersRepository
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import seven.winds.mobi.holiday.users.User
import seven.winds.mobi.holiday.users.UsersRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/callback")
@CrossOrigin(origins = ["*"])
class MessagesController (
        internal val userMessagesRepository: UserMessagesRepository,
        internal val partnerMessagesRepository: PartnerMessagesRepository,
        internal val usersRepository: UsersRepository,
        internal val partnersRepository: PartnersRepository,
        internal val blockedUsersRepository: BlockedUsersRepository,
        internal val blockedPartnersRepository: BlockedPartnersRepository,
        internal val tokensRepository: TokensRepository
) {

    @GetMapping("users/page/{page}")
    fun getUsersMessagesByPage(@PathVariable page: Int, request: HttpServletRequest): ResponseEntity<MutableList<UserMessage>> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(userMessagesRepository.findFirst10ByOrderByTimeDesc(PageRequest.of(page, 10)), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }

    @PostMapping("users/send")
    fun sendUserCallbackMessage(@RequestBody userMessage: UserMessage, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && !blockedUsersRepository.existsByUserId(token.userId)) {
            userMessage.userId = token.userId
            userMessage.time = System.currentTimeMillis()
            userMessagesRepository.save(userMessage)
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @PostMapping("/users")
    fun getListUsers(@RequestBody listUsers: List<Long>, request: HttpServletRequest): ResponseEntity<MutableList<User>> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(usersRepository.findAllByIdInIds(listUsers), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }

    @GetMapping("partners/page/{page}")
    fun getPartnersMessagesByPage(@PathVariable page: Int, request: HttpServletRequest): ResponseEntity<MutableList<PartnerMessage>> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(partnerMessagesRepository.findFirst10ByOrderByTimeDesc(PageRequest.of(page, 10)), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }

    @PostMapping("partners/send")
    fun sendPartnerCallbackMessage(@RequestBody partnerMessage: PartnerMessage, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && !blockedPartnersRepository.existsByPartnerId(token.userId)) {
            partnerMessage.partnerId = token.userId
            partnerMessage.time = System.currentTimeMillis()
            partnerMessagesRepository.save(partnerMessage)
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @PostMapping("/partners")
    fun getListPartners(@RequestBody listPartners: List<Long>, request: HttpServletRequest): ResponseEntity<MutableList<Partner>> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            ResponseEntity(partnersRepository.findAllByIdInIds(listPartners), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
        }
    }
}