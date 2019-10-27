package seven.winds.mobi.holiday.partners

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PartnersRepository : JpaRepository<Partner, Long> {

    fun findOneByEmail(email: String): Partner?

    fun existsByEmail(email: String): Boolean

    @Query("SELECT p FROM Partner p WHERE p.id IN :ids")
    fun findAllByIdInIds(@Param("ids") ids: List<Long>): MutableList<Partner>

    fun findFirst10By(pageable: Pageable): MutableList<Partner>
}