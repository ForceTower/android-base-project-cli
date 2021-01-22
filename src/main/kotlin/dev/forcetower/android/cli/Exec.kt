package dev.forcetower.android.cli

suspend fun main(args: Array<String>) {
    println(args.contentToString())

    val name = "Template"
    val packageName = "dev.ft.template"
    val singleModelName = "Movie"
    val outputPath = "../BasedTemplate"

    TemplateProcessor(name, packageName, singleModelName, outputPath).execute()
}

