package seven.winds.mobi.holiday.blocklist

import org.springframework.data.jpa.repository.JpaRepository

interface BlockedPartnersRepository : JpaRepository<BlockedPartner, Long> {

    fun existsByPartnerId(partnerId: Long): Boolean
}