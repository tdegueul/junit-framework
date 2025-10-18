package junitbuild.compatibility.roseau

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@CacheableTask
abstract class RoseauDiff : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Classpath
    abstract val toolClasspath: ConfigurableFileCollection

    @get:CompileClasspath
    abstract val libraryClasspath: ConfigurableFileCollection

    @get:CompileClasspath
    abstract val v1: RegularFileProperty

    @get:CompileClasspath
    abstract val v2: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val yamlConfiguration: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val acceptedCsv: RegularFileProperty

    @TaskAction
    fun run() {
        val output = ByteArrayOutputStream()
        val result = execOperations.javaexec {
            mainClass = "io.github.alien.roseau.cli.RoseauCLI"
            classpath = toolClasspath
            args(
                "--classpath", libraryClasspath.asPath,
                "--v1", v1.get().asFile.absolutePath,
                "--v2", v2.get().asFile.absolutePath,
                "--diff",
                "--config", yamlConfiguration.get().asFile.absolutePath,
                "--ignored", acceptedCsv.get().asFile.absolutePath,
                "--fail-on-bc"
            )
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            System.out.write(output.toByteArray())
            System.out.flush()
        }
    }
}
