package seven.winds.mobi.holiday.actions

import javax.persistence.*

@Entity
class ActionDemo (
        var title: String,
        var profit: Float,
        var oldCost: Float?,
        var newCost: Float,
        var category: Long,
        var subCategory: Long,
        var image: String,
        var partnerId: Long,
        var latitude: Double?,
        var longitude: Double?,
        var isInteresting: Boolean,
        var isCategoryTop: Boolean,
        var actionBefore: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}