package seven.winds.mobi.holiday.actions

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ActionsDemoRepository : JpaRepository<ActionDemo, Long> {

    fun findFirst10ByLatitudeBetweenAndLongitudeBetweenAndActionBeforeGreaterThanOrderByIdDesc(
            minLat: Double,
            maxLat: Double,
            minLon: Double,
            maxLon: Double,
            time: Long,
            pageable: Pageable
    ): MutableList<ActionDemo>

    fun findFirst10ByLatitudeBetweenAndLongitudeBetweenAndCategoryAndActionBeforeGreaterThanOrderByIdDesc(
            minLat: Double,
            maxLat: Double,
            minLon: Double,
            maxLon: Double,
            category: Long,
            time: Long,
            pageable: Pageable
    ): MutableList<ActionDemo>

    fun findFirst10ByLatitudeBetweenAndLongitudeBetweenAndSubCategoryAndActionBeforeGreaterThanOrderByIdDesc(
            minLat: Double,
            maxLat: Double,
            minLon: Double,
            maxLon: Double,
            subCategory: Long,
            time: Long,
            pageable: Pageable
    ): MutableList<ActionDemo>

    fun findAllByLatitudeBetweenAndLongitudeBetweenAndActionBeforeGreaterThan(
            minLat: Double,
            maxLat: Double,
            minLon: Double,
            maxLon: Double,
            time: Long
    ): MutableList<ActionDemo>

    fun findFirst10By(
            pageable: Pageable
    ): MutableList<ActionDemo>

    fun findAllByPartnerId(partnerId: Long): MutableList<ActionDemo>

    fun findOneByIdAndPartnerId(id: Long, partnerId: Long): ActionDemo?

    fun findAllByTitleContainsIgnoreCaseAndActionBeforeGreaterThan(title: String, actionBefore: Long): MutableList<ActionDemo>
}