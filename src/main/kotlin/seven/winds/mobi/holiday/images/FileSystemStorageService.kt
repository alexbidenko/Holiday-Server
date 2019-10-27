package seven.winds.mobi.holiday.images

import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriter
import java.io.FileInputStream
import java.awt.image.BufferedImage
import java.awt.RenderingHints
import java.awt.Transparency
import kotlin.math.roundToInt
import kotlin.math.sqrt


object FileSystemStorageService {

    private const val location = "images"
    const val cashLocation = "cashImages"

    private val rootLocation = Paths.get(location)
    private val cashRootLocation = Paths.get(cashLocation)

    const val TYPE_CATEGORY_IMAGE = 1
    const val TYPE_ACTION_IMAGE = 2

    init {
        try {
            Files.createDirectories(rootLocation)
            Files.createDirectories(cashRootLocation)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    fun store(file: MultipartFile, filename: String) {
        try {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file $filename")
            }
            if (filename.contains("..")) {
                throw StorageException(
                        "Cannot store file with relative path outside current directory $filename")
            }

            val bytes = file.bytes
            val stream = BufferedOutputStream(FileOutputStream(File(this.cashRootLocation.resolve(filename).toString())))
            stream.write(bytes)
            stream.close()
        } catch (e: IOException) {
            throw StorageException("Failed to store file $filename", e)
        }
    }

    fun loadAsResource(filename: String): Resource {
        try {
            val file = when {
                Files.exists(this.cashRootLocation.resolve(filename)) -> cashRootLocation.resolve(filename)
                Files.exists(this.rootLocation.resolve(filename)) -> rootLocation.resolve(filename)
                else -> null
            }
            if(file != null) {
                val resource = UrlResource(file.toUri())
                if (resource.exists() || resource.isReadable) {
                    return resource
                }
            }
            throw StorageFileNotFoundException(
                    "Could not read file: $filename")
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: $filename", e)
        }

    }

    fun deleteFile(filename: String) {
        if(Files.exists(this.cashRootLocation.resolve(filename))) {
            FileSystemUtils.deleteRecursively(this.cashRootLocation.resolve(filename))
        }
        if(Files.exists(this.rootLocation.resolve(filename))) {
            FileSystemUtils.deleteRecursively(this.rootLocation.resolve(filename))
        }
    }

    fun replaceFile(filename: String, type: Int) {
        if(Files.exists(this.cashRootLocation.resolve(filename))) {
            try {
                val imageFile = File(this.cashRootLocation.resolve(filename).toString())
                val compressedImageFile = File(this.rootLocation.resolve(filename).toString())

                val targetSize = if(type == TYPE_CATEGORY_IMAGE) 8000
                else 600000

                if(imageFile.length() > targetSize) {
                    val inputStream = FileInputStream(imageFile)
                    val outputStream = FileOutputStream(compressedImageFile)

                    val imageQuality = targetSize.toFloat() / imageFile.length()

                    val bufferedImage = ImageIO.read(inputStream)

                    val imageWriters =
                            ImageIO.getImageWritersByFormatName(filename.substringAfterLast("."))

                    val imageWriter = imageWriters.next() as ImageWriter
                    val imageOutputStream = ImageIO.createImageOutputStream(outputStream)
                    imageWriter.output = imageOutputStream

                    val imageWriteParam = imageWriter.defaultWriteParam

                    imageWriter.write(null, IIOImage(scale(bufferedImage, imageQuality), null, null), imageWriteParam)

                    inputStream.close()
                    outputStream.close()
                    imageOutputStream.close()
                    imageWriter.dispose()

                    FileSystemUtils.deleteRecursively(this.cashRootLocation.resolve(filename))
                } else {
                    File(this.cashRootLocation.resolve(filename).toString()).renameTo(
                            File(this.rootLocation.resolve(filename).toString())
                    )
                }
            } catch (e: Exception) {
                File(this.cashRootLocation.resolve(filename).toString()).renameTo(
                        File(this.rootLocation.resolve(filename).toString())
                )
            }
        }
    }

    fun createCopyFile(oldFilename: String, newFilename: String) {
        if(Files.exists(this.rootLocation.resolve(oldFilename))) {
            Files.copy(
                    this.rootLocation.resolve(oldFilename),
                    this.rootLocation.resolve(newFilename)
            )
        }
    }

    private fun scale(image: BufferedImage, imageQuality: Float): BufferedImage {
        val type = if (image.transparency == Transparency.OPAQUE) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB
        var ret = image

        val w = (image.width * sqrt(imageQuality)).roundToInt()
        val h = (image.height * sqrt(imageQuality)).roundToInt()

        val prevW = image.width
        val prevH = image.height

        var scratchImage = BufferedImage(w, h, type)
        var g2 = scratchImage.createGraphics()

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null)

        ret = scratchImage

        g2.dispose()

        if (w != ret.width || h != ret.height) {
            scratchImage = BufferedImage(w, h, type)
            g2 = scratchImage.createGraphics()
            g2.drawImage(ret, 0, 0, null)
            g2.dispose()
            ret = scratchImage
        }

        return ret
    }
}