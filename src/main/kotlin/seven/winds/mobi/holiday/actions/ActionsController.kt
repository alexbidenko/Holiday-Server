package seven.winds.mobi.holiday.actions

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import seven.winds.mobi.holiday.images.FileSystemStorageService
import seven.winds.mobi.holiday.tokens.TokensRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/actions")
@CrossOrigin(origins = ["*"])
class ActionsController (
        internal val actionsDemoRepository: ActionsDemoRepository,
        internal val actionsDetailRepository: ActionsDetailRepository,
        internal val updatedActionRepository: UpdatedActionRepository,
        internal val tokensRepository: TokensRepository
) {

    val storageService = FileSystemStorageService

    @GetMapping("/page")
    fun getActionsByPage(
            @RequestParam("page") page: Int,
            @RequestParam("userLatitude", required = false) userLatitude: Double?,
            @RequestParam("userLongitude", required = false) userLongitude: Double?,
            @RequestParam("radius", required = false) radius: Double?,
            @RequestParam("category", required = false) category: Long?,
            @RequestParam("subCategory", required = false) subCategory: Long?
    ): MutableList<ActionDemo> {
        return if(userLatitude != null && userLongitude != null && radius != null) {
            when {
                subCategory != null -> actionsDemoRepository.findFirst10ByLatitudeBetweenAndLongitudeBetweenAndSubCategoryAndActionBeforeGreaterThanOrderByIdDesc(
                        userLatitude - radius,
                        userLatitude + radius,
                        userLongitude - radius,
                        userLongitude + radius,
                        subCategory,
                        System.currentTimeMillis(),
                        PageRequest.of(page, 10)
                )
                category != null -> actionsDemoRepository.findFirst10ByLatitudeBetweenAndLongitudeBetweenAndCategoryAndActionBeforeGreaterThanOrderByIdDesc(
                        userLatitude - radius,
                        userLatitude + radius,
                        userLongitude - radius,
                        userLongitude + radius,
                        category,
                        System.currentTimeMillis(),
                        PageRequest.of(page, 10)
                )
                else -> actionsDemoRepository.findFirst10ByLatitudeBetweenAndLongitudeBetweenAndActionBeforeGreaterThanOrderByIdDesc(
                        userLatitude - radius,
                        userLatitude + radius,
                        userLongitude - radius,
                        userLongitude + radius,
                        System.currentTimeMillis(),
                        PageRequest.of(page, 10)
                )
            }
        } else {
            actionsDemoRepository.findFirst10By(
                    PageRequest.of(page, 10)
            )
        }
    }

    @GetMapping("/coordinates")
    fun getActionsByCoordinates(
            @RequestParam("latitude") latitude: Double,
            @RequestParam("longitude") longitude: Double,
            @RequestParam("radius") radius: Double
    ): MutableList<ActionDemo> {
        return actionsDemoRepository.findAllByLatitudeBetweenAndLongitudeBetweenAndActionBeforeGreaterThan(
                latitude - radius,
                latitude + radius,
                longitude - radius,
                longitude + radius,
                System.currentTimeMillis()
        )
    }

    @GetMapping("/partner/{partnerId}")
    fun getActionsByPartner(@PathVariable partnerId: Long): ResponseEntity<MutableList<ActionDemo>> {
        return ResponseEntity(actionsDemoRepository.findAllByPartnerId(partnerId), HttpStatus.OK)
    }

    @GetMapping("/detail/{id}")
    fun getDetailAction(@PathVariable("id") id: Long): ResponseEntity<Any> {
        val actionDetail = actionsDetailRepository.findByActionId(id)
        return ResponseEntity(actionDetail, HttpStatus.OK)
    }

    @PostMapping("/add")
    fun addNewAction(@RequestBody newAction: NewAction, request: HttpServletRequest): ResponseEntity<Any> {
        newAction.createTime = System.currentTimeMillis()
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null) {
            newAction.partnerId = token.userId
            updatedActionRepository.save(newAction)
            storageService.replaceFile(newAction.image, storageService.TYPE_ACTION_IMAGE)
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteAction(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null) {
            val actionDemo = actionsDemoRepository.findOneByIdAndPartnerId(id, token.userId)
            if(actionDemo != null) {
                storageService.deleteFile(actionDemo.image)
                actionsDemoRepository.deleteById(id)
                val actionDetail = actionsDetailRepository.findByActionId(id)
                actionsDetailRepository.deleteById(actionDetail.id!!)
                ResponseEntity(true, HttpStatus.OK)
            }
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @PutMapping("/update/{updatedId}")
    fun updateAction(
            @RequestBody newAction: NewAction,
            @PathVariable("updatedId") updatedId: Long,
            request: HttpServletRequest
    ): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null) {
            newAction.partnerId = token.userId
            newAction.timeUpdate = System.currentTimeMillis()
            newAction.originId = updatedId
            updatedActionRepository.save(newAction)

            val oldAction = actionsDemoRepository.getOne(updatedId)
            if(oldAction.image == newAction.image) {
                storageService.createCopyFile(
                        oldAction.image,
                        "${token.id}-${token.userId}-${System.currentTimeMillis()}." +
                                oldAction.image.substringAfterLast(".")
                )
            } else {
                storageService.replaceFile(newAction.image, storageService.TYPE_ACTION_IMAGE)
            }
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping("/updated")
    fun getUpdated(
            @RequestParam("originId", defaultValue = "0") originId: Long,
            @RequestParam("partnerId", defaultValue = "0") partnerId: Long
    ): ResponseEntity<Any> {
        return when {
            originId != 0L -> ResponseEntity(updatedActionRepository.findOneByOriginId(originId), HttpStatus.OK)
            partnerId != 0L -> ResponseEntity(updatedActionRepository.findAllByPartnerId(partnerId), HttpStatus.OK)
            else -> ResponseEntity(updatedActionRepository.findAll(), HttpStatus.OK)
        }
    }

    @DeleteMapping("/updated/delete/{id}")
    fun deleteUpdatedAction(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<Any> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null) {
            val action = updatedActionRepository.getOne(id)
            storageService.deleteFile(action.image)
            updatedActionRepository.deleteById(id)
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @GetMapping("/address/{text}")
    fun getAddressByText(@PathVariable text: String): ResponseEntity<String> {
        val restEntity =
                RestTemplate().getForEntity("https://geocoder.api.here.com/6.2/geocode.json?searchtext=$text&app_id=UdRH6PlISTlADYsW6mzl&app_code=lfrrTheP9nBedeJyy1NtIA&gen=8", String::class.java)
        val mapper = ObjectMapper()
        val response = restEntity.body
        val root = mapper.readTree(response)
        val location = try {
            root.get("Response").get("View").get(0).get("Result").get(0).get("Location").get("DisplayPosition")
        } catch (e: Exception) {
            mapper.readTree("""{"isFind":false}""")
        }
        return ResponseEntity(location.toString(), HttpStatus.OK)
    }

    @GetMapping("/search/{text}")
    fun searchActionsDemo(@PathVariable text: String): ResponseEntity<MutableList<ActionDemo>> {
        return ResponseEntity(actionsDemoRepository
                .findAllByTitleContainsIgnoreCaseAndActionBeforeGreaterThan(text, System.currentTimeMillis()), HttpStatus.OK)
    }
}