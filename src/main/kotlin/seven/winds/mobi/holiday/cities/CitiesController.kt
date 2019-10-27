package seven.winds.mobi.holiday.cities

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cities")
class CitiesController (
        internal val citiesRepository: CitiesRepository
) {

    @GetMapping("/get")
    fun getCities(): ResponseEntity<MutableList<City>> {
        return ResponseEntity(citiesRepository.findAll(), HttpStatus.OK)
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addCity(@RequestBody newCity: City) {
        citiesRepository.save(newCity)
    }

    @PostMapping("/add-all")
    @ResponseStatus(HttpStatus.CREATED)
    fun addAllCities(@RequestBody newCities: List<City>) {
        citiesRepository.saveAll(newCities)
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCity(@PathVariable id: Long, @RequestBody newCity: City) {
        val city = citiesRepository.getOne(id)
        city.city = newCity.city
        city.latitude = newCity.latitude
        city.longitude = newCity.longitude
        citiesRepository.save(city)
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteCity(@PathVariable id: Long) {
        citiesRepository.deleteById(id)
    }
}