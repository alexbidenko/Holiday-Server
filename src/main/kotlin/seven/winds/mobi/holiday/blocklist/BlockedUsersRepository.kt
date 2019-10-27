package seven.winds.mobi.holiday.blocklist

import org.springframework.data.jpa.repository.JpaRepository

interface BlockedUsersRepository : JpaRepository<BlockedUser, Long> {

    fun existsByUserId(userId: Long): Boolean
}