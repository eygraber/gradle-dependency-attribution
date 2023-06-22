import com.eygraber.dependency.attribution.DependencyAttributionTask
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonProjectExtension

plugins.withId("org.jetbrains.kotlin.jvm") {
  val task = tasks.register<DependencyAttributionTask>("generateDependencyAttributionProto") {
    rootComponent.set(
      project
        .configurations
        .getByName("runtimeClasspath")
        .incoming
        .resolutionResult
        .rootComponent
    )

    outputDirectory.set(
      layout.buildDirectory.dir(
        "generated/dependency-attribution/resources"
      )
    )

    outputFilename.set("dependency_attribution.proto")
  }

  extensions.configure<KotlinCommonProjectExtension> {
    sourceSets.getByName("main").resources.srcDir(task.map { it.outputDirectory })
  }
}
