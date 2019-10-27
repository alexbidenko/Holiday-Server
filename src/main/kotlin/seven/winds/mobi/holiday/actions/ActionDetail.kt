package seven.winds.mobi.holiday.actions

import seven.winds.mobi.holiday.objects.BetweenTime
import seven.winds.mobi.holiday.objects.HashMapConverter
import javax.persistence.*

@Entity
class ActionDetail (
        var actionId: Long,
        var address: String,
        var phone: String,
        @Convert(converter = HashMapConverter::class)
        var workTime: Map<String, Any>,
        var site: String?,

        @Column(length = 100000)
        @Lob
        var information: String?,
        @Convert(converter = HashMapConverter::class)
        var socialNetworks: Map<String, String?>,
        var createTime: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}