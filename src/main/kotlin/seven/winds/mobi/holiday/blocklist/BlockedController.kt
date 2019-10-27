package seven.winds.mobi.holiday.blocklist

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/blocklist")
@CrossOrigin(origins = ["*"])
class BlockedController (
        internal val blockedUsersRepository: BlockedUsersRepository,
        internal val blockedPartnersRepository: BlockedPartnersRepository,
        internal val tokensRepository: TokensRepository
) {
    
    @GetMapping("/block/user/{userId}")
    fun blockUser(@PathVariable userId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            blockedUsersRepository.save(BlockedUser(userId))
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping("/block/partner/{partnerId}")
    fun blockPartner(@PathVariable partnerId: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && token.status == TokensFunction.STATUS_ADMINISTRATOR) {
            blockedPartnersRepository.save(BlockedPartner(partnerId))
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }
}