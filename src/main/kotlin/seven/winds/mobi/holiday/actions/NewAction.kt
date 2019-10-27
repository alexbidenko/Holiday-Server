package seven.winds.mobi.holiday.actions

import seven.winds.mobi.holiday.objects.HashMapConverter
import javax.persistence.*

@Entity
class NewAction (
        var title: String,
        var profit: Float,
        var oldCost: Float?,
        var newCost: Float,
        var address: String,
        var phone: String,
        var category: Long,
        var subCategory: Long,
        @Convert(converter = HashMapConverter::class)
        var workTime: Map<String, Any>,
        var image: String,
        var site: String?,

        @Column(length = 100000)
        @Lob
        var information: String?,
        @Convert(converter = HashMapConverter::class)
        var socialNetworks: Map<String, String?>,
        var createTime: Long,
        var isInteresting: Boolean,
        var isCategoryTop: Boolean,
        var actionBefore: Long
) {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long? = null

        var partnerId: Long = 0
        var originId: Long = 0
        var timeUpdate: Long = 0
}