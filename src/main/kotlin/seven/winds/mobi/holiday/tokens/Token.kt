package seven.winds.mobi.holiday.tokens

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Token (
        var value: String,
        var userId: Long,
        var time: Long,
        var status: Byte
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0
}