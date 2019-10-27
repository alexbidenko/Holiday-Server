package seven.winds.mobi.holiday.users

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UsersRepository : JpaRepository<User, Long> {

    fun findOneByLogin(login: String): User?

    fun existsByLogin(login: String): Boolean

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    fun findAllByIdInIds(@Param("ids") ids: List<Long>): MutableList<User>

    fun findFirst10By(pageable: Pageable): MutableList<User>
}