package seven.winds.mobi.holiday.callback

import javax.persistence.*

@Entity
class PartnerMessage (
        @Column(length = 512)
        @Lob
        val message: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
    var partnerId: Long = 0
    var time: Long = 0
    var isSeen: Boolean = false
}