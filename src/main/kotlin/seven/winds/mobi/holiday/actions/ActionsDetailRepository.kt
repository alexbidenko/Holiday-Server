package seven.winds.mobi.holiday.actions

import org.springframework.data.jpa.repository.JpaRepository

interface ActionsDetailRepository : JpaRepository<ActionDetail, Long> {

    fun findByActionId(actionId: Long): ActionDetail
}