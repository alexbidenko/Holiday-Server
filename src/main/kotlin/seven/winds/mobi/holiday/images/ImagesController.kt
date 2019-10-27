package seven.winds.mobi.holiday.images

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.GetMapping
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = ["*"])
class ImagesController (
        internal val tokensRepository: TokensRepository
) {

    val storageService = FileSystemStorageService

    @GetMapping("/get/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val file = storageService.loadAsResource(filename)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.filename + "\"").body(file)
    }

    @PostMapping("/add")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile,
                         request: HttpServletRequest): ResponseEntity<String> {
        val token = tokensRepository.findOneByValue(request.getHeader("Token"))
        return if(token != null && (token.status == TokensFunction.STATUS_PARTNER || token.status == TokensFunction.STATUS_ADMINISTRATOR)) {
            val mine = file.originalFilename!!.substringAfterLast(".")
            if (mine != "jpg" && mine != "jpeg" && mine != "png") {
                ResponseEntity(HttpStatus.CONFLICT)
            } else {
                val filename =
                        "${token.id}-${token.userId}-${System.currentTimeMillis()}." +
                                file.originalFilename!!.substringAfterLast(".")
                storageService.store(file, filename)
                ResponseEntity("""{"filename":"$filename"}""", HttpStatus.OK)
            }
        } else ResponseEntity(HttpStatus.CONFLICT)
    }
}