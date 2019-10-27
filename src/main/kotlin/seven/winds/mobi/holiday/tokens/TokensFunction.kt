package seven.winds.mobi.holiday.tokens

object TokensFunction {
    const val STATUS_USER: Byte = 1
    const val STATUS_PARTNER: Byte = 2
    const val STATUS_ADMINISTRATOR: Byte = 13

    fun checkTokenStatus(tokenValue: String, tokensRepository: TokensRepository): Byte {
        val token = tokensRepository.findOneByValue(tokenValue)
        return token?.status ?: 0
    }
}