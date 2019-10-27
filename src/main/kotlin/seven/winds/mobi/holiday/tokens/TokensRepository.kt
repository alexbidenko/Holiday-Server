package seven.winds.mobi.holiday.tokens

import org.springframework.data.jpa.repository.JpaRepository

interface TokensRepository : JpaRepository<Token, Long> {

    fun findOneByValue(value: String): Token?

    fun deleteAllByTimeLessThan(time: Long)
}