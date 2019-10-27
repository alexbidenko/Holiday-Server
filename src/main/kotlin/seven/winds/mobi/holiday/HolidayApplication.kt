package seven.winds.mobi.holiday

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HolidayApplication

fun main(args: Array<String>) {
    runApplication<HolidayApplication>(*args)
    // nohup java -jar holiday-0.4.1.jar 2>&1 >> holiday.log &
    // 22314
    /*

security.require-ssl=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=server-holiday
server.ssl.key-alias=tomcat*/
}
