package seven.winds.mobi.holiday.objects

import java.io.IOException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.persistence.AttributeConverter

class HashMapConverter : AttributeConverter<Map<String, Any>, String> {

    override fun convertToDatabaseColumn(customerInfo: Map<String, Any>): String? {

        var customerInfoJson: String? = null
        try {
            customerInfoJson = jacksonObjectMapper().writeValueAsString(customerInfo)
        } catch (e: JsonProcessingException) {}

        return customerInfoJson
    }

    override fun convertToEntityAttribute(customerInfoJSON: String): Map<String, Any>? {
        var customerInfo: Map<String, Any>? = null
        try {
            customerInfo = jacksonObjectMapper().readValue(customerInfoJSON)
        } catch (e: IOException) {}

        return customerInfo
    }

}