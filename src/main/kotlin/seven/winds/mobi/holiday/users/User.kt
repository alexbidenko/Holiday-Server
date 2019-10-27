package seven.winds.mobi.holiday.users

import javax.persistence.*

@Entity
class User (
        @Column(length = 64)
        @Lob
        var name: String,
        @Column(length = 32)
        @Lob
        var login: String,
        var email: String,
        var phone: String,
        var birthday: Long,
        var password: String,
        var typeAuthorization: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
    var registrationTime: Long = 0
    var lastActiveTime: Long = 0
}