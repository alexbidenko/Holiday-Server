package seven.winds.mobi.holiday.categories

import org.springframework.data.jpa.repository.JpaRepository

interface SubCategoriesRepository : JpaRepository<SubCategory, Long> {

    fun findAllByCategoryId(id: Long): MutableList<SubCategory>
}