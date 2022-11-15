package nl.stokpop.wiremock.generator

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.listDirectoryEntries

@Component
class FileStorage(val filesPath: Path = Path.of("/tmp")) {

    fun File.bufferedOutputStream(size: Int = 8192) = BufferedOutputStream(this.outputStream(), size)
    fun File.zipOutputStream(size: Int = 8192) = ZipOutputStream(this.bufferedOutputStream(size))
    fun File.bufferedInputStream(size: Int = 8192) = BufferedInputStream(this.inputStream(), size)
    fun File.asZipEntry() = ZipEntry(this.name)

    fun createProject(file: MultipartFile, projectId: String): Path {
        val workDir = filesPath.resolve(projectId)
        Files.createDirectories(workDir)
        val swaggerFile = workDir.resolve("swagger.json")
        Files.copy(file.inputStream, swaggerFile)
        return workDir
    }

    fun zip(workDir: Path, projectId: String): Path {
        val list = workDir.listDirectoryEntries()
        val zipFile = workDir.resolve("$projectId.zip").toFile()

        zipFile.zipOutputStream().use {
            list.map { p -> p.toFile() }
                .filter { f -> !f.isDirectory }
                .forEach { file ->
                    it.putNextEntry(file.asZipEntry())
                    file.bufferedInputStream().use { bis -> bis.copyTo(it) }
                }
        }
        return zipFile.toPath()
    }

    fun fileAsResource(file: Path): Resource {
        return UrlResource(file.toUri())
    }

}
