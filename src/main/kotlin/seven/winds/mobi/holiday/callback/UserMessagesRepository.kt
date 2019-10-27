package seven.winds.mobi.holiday.callback

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserMessagesRepository : JpaRepository<UserMessage, Long> {

    fun findFirst10ByOrderByTimeDesc(pageable: Pageable): MutableList<UserMessage>
}