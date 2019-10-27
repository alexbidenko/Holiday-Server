package seven.winds.mobi.holiday.categories

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import seven.winds.mobi.holiday.images.FileSystemStorageService
import seven.winds.mobi.holiday.tokens.TokensFunction
import seven.winds.mobi.holiday.tokens.TokensFunction.checkTokenStatus
import seven.winds.mobi.holiday.tokens.TokensRepository
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = ["*"])
class CategoriesController (
        internal val categoriesRepository: CategoriesRepository,
        internal val subCategoriesRepository: SubCategoriesRepository,
        internal val tokensRepository: TokensRepository
) {

    var storageService = FileSystemStorageService

    @GetMapping("/all")
    fun getAllCategories(): ResponseEntity<MutableList<Category>> {
        return ResponseEntity(categoriesRepository.findAll(), HttpStatus.OK)
    }

    @GetMapping("/sub/all")
    fun getAllSubCategories(): ResponseEntity<MutableList<SubCategory>> {
        return ResponseEntity(subCategoriesRepository.findAll(), HttpStatus.OK)
    }

    @GetMapping("/sub/{id}")
    fun getSubCategories(@PathVariable id: Long): ResponseEntity<MutableList<SubCategory>> {
        return ResponseEntity(subCategoriesRepository.findAllByCategoryId(id), HttpStatus.OK)
    }

    @PostMapping("/add")
    fun addCategory(@RequestBody newCategory: Category, request: HttpServletRequest): ResponseEntity<String> {
        return if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            storageService.replaceFile(newCategory.image, storageService.TYPE_CATEGORY_IMAGE)
            categoriesRepository.save(newCategory)
            return ResponseEntity("""{"id":${newCategory.id}}""", HttpStatus.CREATED)
        } else ResponseEntity(HttpStatus.CONFLICT)
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCategory(@PathVariable id: Long, @RequestBody newCategory: Category, request: HttpServletRequest) {
        if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            val category = categoriesRepository.getOne(id)
            category.title = newCategory.title
            if(category.image != newCategory.image) {
                storageService.deleteFile(category.image)
                storageService.replaceFile(newCategory.image, storageService.TYPE_CATEGORY_IMAGE)
            }
            category.image = newCategory.image
            categoriesRepository.save(category)
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteCategory(@PathVariable id: Long, request: HttpServletRequest) {
        if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            val category = categoriesRepository.getOne(id)
            storageService.deleteFile(category.image)
            categoriesRepository.deleteById(id)

            val subCategories = subCategoriesRepository.findAllByCategoryId(id)
            subCategories.forEach {
                it.categoryId = 0
                subCategoriesRepository.save(it)
            }
        }
    }

    @PostMapping("/sub/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addSubCategory(@RequestBody newSubCategory: SubCategory, request: HttpServletRequest): ResponseEntity<String> {
        return if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            subCategoriesRepository.save(newSubCategory)
            ResponseEntity("""{"id":${newSubCategory.id}}""", HttpStatus.CREATED)
        } else ResponseEntity(HttpStatus.CONFLICT)
    }

    @PutMapping("/sub/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateSubCategory(@PathVariable id: Long, @RequestBody newSubCategory: SubCategory, request: HttpServletRequest) {
        if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            val subCategory = subCategoriesRepository.getOne(id)
            subCategory.title = newSubCategory.title
            subCategoriesRepository.save(subCategory)
        }
    }

    @DeleteMapping("/sub/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteSubCategory(@PathVariable id: Long, request: HttpServletRequest) {
        if(checkTokenStatus(request.getHeader("Token"), tokensRepository) == TokensFunction.STATUS_ADMINISTRATOR) {
            subCategoriesRepository.deleteById(id)
        }
    }
}