package dev.forcetower.android.cli

import java.io.File

class TemplateProcessor constructor(
    private val name: String = "Template",
    private val packageName: String = "dev.ft.template",
    private val singleModelName: String = "Movie",
    private val outputPath: String = "BasedFolder",
) {
    suspend fun execute() {
        // Step: clone
        val clone = CommandRunner.exec("git clone --recurse-submodules -j8 --depth 1 https://github.com/ForceTower/android-basic-template.git $outputPath")
        if (clone != 0) throw IllegalStateException("Failed to clone with status: $clone")

        val outputFolder = File(outputPath)

        // Step: remove old .git file
        val mainGit = File(outputFolder, ".git")
        mainGit.deleteRecursively()

        // Step: manifest changes
        val manifestFile = File(outputFolder, "app/src/main/AndroidManifest.xml")
        val nextManifestText = manifestFile.readText()
            .replace("dev.forcetower.application", packageName)
            .replace("_M_NAME_", singleModelName)
            .replace("_P_NAME_", name)
        manifestFile.writeText(nextManifestText)

        // Step: build.gradle changes
        val buildGradleFile = File(outputFolder, "app/build.gradle")
        val nextBuildGradleText = buildGradleFile.readText()
            .replace("dev.forcetower.application", packageName)
            .replace("_M_NAME_", singleModelName)
            .replace("_P_NAME_", name)
        buildGradleFile.writeText(nextBuildGradleText)

        // Step: settings.gradle changes
        val settingsGradleFile = File(outputFolder, "settings.gradle")
        val nextSettingsGradleText = settingsGradleFile.readText()
            .replace("dev.forcetower.application", packageName)
            .replace("_M_NAME_", singleModelName)
            .replace("_P_NAME_", name)
        settingsGradleFile.writeText(nextSettingsGradleText)

        // Step: prepare to copy to new package
        val initialPackageFolder = File(outputFolder, "app/src/main/java/dev/forcetower/application")
        val nextPackageFolder = File(outputFolder, "app/src/main/java/${packageName.replace(".", "/")}")
        moveFilesAndChangeContent(initialPackageFolder, nextPackageFolder)

        // Step: test folders
        val initialTestPackageFolder = File(outputFolder, "app/src/test/java/dev/forcetower/application")
        val nextTestPackageFolder = File(outputFolder, "app/src/test/java/${packageName.replace(".", "/")}")
        moveFilesAndChangeContent(initialTestPackageFolder, nextTestPackageFolder)

        // Step: androidTest folders
        val initialAndroidTestPackageFolder = File(outputFolder, "app/src/androidTest/java/dev/forcetower/application")
        val nextAndroidTestPackageFolder = File(outputFolder, "app/src/androidTest/java/${packageName.replace(".", "/")}")
        moveFilesAndChangeContent(initialAndroidTestPackageFolder, nextAndroidTestPackageFolder)
    }

    private fun moveFilesAndChangeContent(source: File, target: File) {
        target.mkdirs()

        // Step: copy files
        source.copyRecursively(target, onError = { file, ioException ->
            println(file)
            ioException.printStackTrace()
            OnErrorAction.TERMINATE
        })

        // Step: delete old files
        source.deleteRecursively()

        // Step: delete leftover folders
        var deleteOld = source.parentFile
        var fullfilled = false
        while (deleteOld != null && !fullfilled) {
            val files = deleteOld.listFiles()
            if (files == null || files.isEmpty()) {
                deleteOld.delete()
            } else {
                fullfilled = true
            }
            deleteOld = deleteOld.parentFile
        }

        // Step: make file changes
        target.walkTopDown()
            .forEach {
                evaluateFile(it)
            }

        // Step: change folders names
        target.walkTopDown()
            .forEach {
                evaluateFolder(it)
            }
    }

    private fun evaluateFolder(folder: File) {
        if (folder.isFile) return
        if (folder.name.contains("_M_NAME_") || folder.name.contains("_P_NAME_")) {
            val child = folder.name
                .replace("_M_NAME_", singleModelName.toLowerCase())
                .replace("_P_NAME_", name.toLowerCase())

            val ff = File(folder.parentFile, child)
            folder.renameTo(ff)
        }
    }

    private fun evaluateFile(file: File) {
        if (file.isDirectory) return
        // Default Checks
        var usable = file
        if (file.name.contains("_M_NAME_") || file.name.contains("_P_NAME_")) {
            val child = file.name
                .replace("_M_NAME_", singleModelName)
                .replace("_P_NAME_", name)

            val ff = File(file.parentFile, child)
            file.renameTo(ff)
            usable = ff
        }

        val next = usable.readText()
            .replace(Regex("package dev\\.forcetower\\.application\\.([a-zA-Z\\.]*)(_M_NAME_)")) {
                it.value.replace("_M_NAME_", singleModelName.toLowerCase())
            }
            .replace("dev.forcetower.application", packageName)
            .replace("._M_NAME_.", ".${singleModelName.toLowerCase()}.")
            .replace("._M_NAME_\n", ".$singleModelName\n")
            .replace("._M_NAME_", ".$singleModelName")
            .replace("_M_NAME_", singleModelName)
            .replace("_P_NAME_", name)

        usable.writeText(next)
    }
}