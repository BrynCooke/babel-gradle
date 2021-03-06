package com.github.bryncooke.babelgradle

import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class BabelTask extends DefaultTask {

    @Input
    String[] presets

    @Input
    String[] fileExtensions = ["js", "jsx"]

    @InputDirectory
    File inputDir = project.file("src/main/babel")

    @OutputDirectory
    File outputDir = project.file("build/resources/babel")

    BabelTask() {
        description = "Process babel resources"
        group = "Build"
    }

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {

        if (!inputs.incremental) {
            project.delete(outputDir.listFiles())
        }

        def filesToProcess = []
        //Work out which files to process
        //Just copy files without the right extension
        inputs.outOfDate { change ->
            def srcFile = project.file("$inputDir/${change.file.name}")
            if (fileExtensions.find { srcFile.name.endsWith('.' + it) } != null) {
                filesToProcess.add(inputDir.toPath().relativize(srcFile.toPath()).toString())
            } else {
                project.copy { e ->
                    e.from srcFile
                    e.into outputDir
                }
            }
        }

        //Process the files
        if (!filesToProcess.isEmpty()) {
            logging.captureStandardOutput(LogLevel.INFO)
            def commandLine = ['npx', 'babel', *filesToProcess, '-d', outputDir.absoluteFile.path, '--presets=' + presets.join(','), '--source-maps']
            project.exec { e ->
                e.workingDir inputDir
                e.commandLine commandLine
            }
        }

        //Remove files that need to be removed
        inputs.removed { change ->
            def targetFile = project.file("$outputDir/${change.file.name.replace(".jsx", ".js")}")
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }
    }
}
