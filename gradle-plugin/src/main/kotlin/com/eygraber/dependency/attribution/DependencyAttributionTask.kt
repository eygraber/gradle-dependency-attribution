package com.eygraber.dependency.attribution

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class DependencyAttributionTask : DefaultTask() {
  @get:Inject abstract val dependencyHandler: DependencyHandler

  @get:Input
  abstract val rootComponent: Property<ResolvedComponentResult>

  @get:Internal
  abstract val outputFilename: Property<String>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun writeDependencyAttributions() {
    val data = rootComponent.get().serializeDependenciesProto(
      dependencyHandler = dependencyHandler,
      logger = logger
    )

    data.encode(
      File(outputDirectory.get().asFile, outputFilename.get()).outputStream()
    )
  }
}
