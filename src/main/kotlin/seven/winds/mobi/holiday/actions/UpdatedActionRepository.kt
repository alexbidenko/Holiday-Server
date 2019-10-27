package seven.winds.mobi.holiday.actions

import org.springframework.data.jpa.repository.JpaRepository

interface UpdatedActionRepository : JpaRepository<NewAction, Long> {

    fun findAllByPartnerId(partnerId: Long): MutableList<NewAction>

    fun findOneByOriginId(originId: Long): NewAction

    fun findOneByImage(image: String): NewAction?
}