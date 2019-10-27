package seven.winds.mobi.holiday.partners

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import seven.winds.mobi.holiday.objects.Authorization
import seven.winds.mobi.holiday.objects.ChangePassword
import seven.winds.mobi.holiday.tokens.Token
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/partners")
class PartnersController (
        internal val partnersRepository: PartnersRepository,
        internal val tokensRepository: TokensRepository
) {

    @PostMapping("/registration")
    fun registrationPartner(@RequestBody newPartner: Partner, request: HttpServletRequest): ResponseEntity<Any> {
        if(partnersRepository.existsByEmail(newPartner.email)) return ResponseEntity(HttpStatus.CONFLICT)
        val encoder = BCryptPasswordEncoder()
        newPartner.password = encoder.encode(newPartner.password)
        newPartner.registrationTime = System.currentTimeMillis()
        newPartner.lastActiveTime = System.currentTimeMillis()
        partnersRepository.save(newPartner)
        val tokenValue = encoder.encode("holiday_partner_" + newPartner.id + "_" + request.getHeader("User-Agent"))
        tokensRepository.save(Token(
                tokenValue,
                newPartner.id!!,
                System.currentTimeMillis(),
                TokensFunction.STATUS_PARTNER
        ))
        return ResponseEntity("""{"token":"$tokenValue","id":${newPartner.id}}""", HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun loginPartner(@RequestBody authorization: Authorization, request: HttpServletRequest): ResponseEntity<Any> {
        val encoder = BCryptPasswordEncoder()
        val partner = partnersRepository.findOneByEmail(authorization.email!!)
        return when {
            partner == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            encoder.matches(authorization.password, partner.password) -> {
                val tokenValue = encoder.encode("holiday_partner_" + partner.id + "_" + request.getHeader("User-Agent"))
                val token = tokensRepository.findOneByValue(tokenValue)
                if(token != null) {
                    token.time = System.currentTimeMillis()
                    tokensRepository.save(token)
                } else {
                    tokensRepository.save(Token(
                            tokenValue,
                            partner.id!!,
                            System.currentTimeMillis(),
                            TokensFunction.STATUS_PARTNER
                    ))
                }
                partner.lastActiveTime = System.currentTimeMillis()
                partnersRepository.save(partner)
                ResponseEntity(mapOf("partner" to partner, "token" to tokenValue), HttpStatus.OK)
            }
            else -> ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @PutMapping("/update")
    fun updatePartner(@RequestBody newPartner: Partner, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        val partner = if(token != null) partnersRepository.getOne(token.userId) else null
        return when (partner) {
            null -> ResponseEntity(HttpStatus.NOT_FOUND)
            else -> {
                partner.firstName = newPartner.firstName
                partner.lastName = newPartner.lastName
                partner.middleName = newPartner.middleName
                partner.email = newPartner.email
                partnersRepository.save(partner)
                ResponseEntity(HttpStatus.OK)
            }
        }
    }

    @PutMapping("/password")
    fun changePartnerPassword(@RequestBody changePassword: ChangePassword, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        val encoder = BCryptPasswordEncoder()
        val partner = if(token != null) partnersRepository.getOne(token.userId) else null
        return when {
            partner == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            encoder.matches(changePassword.oldPassword, partner.password) -> {
                partner.password = encoder.encode(changePassword.newPassword)
                partnersRepository.save(partner)
                ResponseEntity(HttpStatus.OK)
            }
            else -> ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @GetMapping("/check")
    fun checkPartner(request: HttpServletRequest): ResponseEntity<Any> {
        return if(tokensRepository.findOneByValue(request.getHeader("Token")) != null) {
            ResponseEntity("""{"is":true}""", HttpStatus.OK)
        } else {
            ResponseEntity("""{"is":false}""", HttpStatus.OK)
        }
    }

    /*@GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logoutPartner(httpSession: HttpSession) {
        httpSession.removeAttribute("partner")
    }*/
}