package seven.winds.mobi.holiday.secret

import org.springframework.data.jpa.repository.JpaRepository

interface SecretsRepository : JpaRepository<Secret, Long> {

    fun findOneByValue(value: String): Secret?

    fun deleteByValue(value: String)

    fun deleteAllByTimeLessThan(time: Long)
}