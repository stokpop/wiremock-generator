package nl.stokpop.wiremock.generator

import nl.stokpop.jmeter.generator.WiremockGeneratorException
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

@RestController
class JmeterGeneratorController(val storage: FileStorage) {

    val zipFiles: ConcurrentHashMap<String, Path> = ConcurrentHashMap()

    @PostMapping("/upload")
    fun upload(@RequestParam file: MultipartFile): FileUploadResponse {
        // based on: https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
        val projectId = "wiremock-gen." + System.currentTimeMillis()
        val workDir = storage.createProject(file, projectId)

        // this should be done in a co routine
        generateAndZipAsync(workDir, projectId)

        // now return zip download url
        return FileUploadResponse(projectId)
    }

    private fun generateAndZipAsync(workDir: Path, projectId: String) {
        // now call generator
        executeCommand(
            workDir,
            "java -jar /openapi-generator-cli.jar generate -i swagger.json -g wiremock -o mappings"
        )

        // now zip generated mappings
        val zipFile = storage.zip(workDir.resolve("mappings"), projectId)
        // keep in mapping for future download
        zipFiles[projectId] = zipFile
    }

    private fun executeCommand(workDir: Path, command: String) {
        val process = ProcessBuilder(command.split(" "))
            .directory(workDir.toFile())
            .redirectErrorStream(true)
            .start()

        try {
            val hasRunToExit = process.waitFor(5, TimeUnit.MINUTES)

            if (!hasRunToExit) throw WiremockGeneratorException()

            val exitValue = process.exitValue()

            if (exitValue != 0) {
                throw WiremockGeneratorException()
            }
        } finally {
            val output = extractOutput(process)
            print(output)
            process.destroy()
        }
    }

    private fun extractOutput(process: Process): String {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = reader.lines().collect(Collectors.joining(System.getProperty("line.separator")))
        return output
    }

    @GetMapping("/download/{projectId:.+}")
    fun download(@PathVariable projectId: String, request: HttpServletRequest): ResponseEntity<Resource> {

        val zipFile = zipFiles[projectId] ?: throw RuntimeException("Zip file for $projectId unknown or not ready.")

        val resource = storage.fileAsResource(zipFile)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$projectId.zip")
            .body(resource)
    }
}
