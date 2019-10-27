package seven.winds.mobi.holiday.callback

import javax.persistence.*

@Entity
class UserMessage (
        @Column(length = 256)
        @Lob
        val message: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
    var userId: Long = 0
    var time: Long = 0
    var isSeen: Boolean = false
}