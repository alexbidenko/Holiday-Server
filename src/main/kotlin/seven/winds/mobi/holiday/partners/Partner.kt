package seven.winds.mobi.holiday.partners

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Partner (
        var firstName: String,
        var lastName: String,
        var middleName: String,
        var email: String,
        var password: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
    var registrationTime: Long = 0
    var lastActiveTime: Long = 0
}