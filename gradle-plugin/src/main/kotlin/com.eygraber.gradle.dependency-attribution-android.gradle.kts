import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.eygraber.dependency.attribution.DependencyAttributionTask
import java.util.Locale

plugins.withId("com.android.application") {
  with(extensions.getByType<ApplicationAndroidComponentsExtension>()) {
    onVariants { variant ->
      @Suppress("DEPRECATION")
      val nameForTasks = variant.name.capitalize(Locale.US)

      val task = tasks.register<DependencyAttributionTask>(
        "generate${nameForTasks}DependencyAttributionProto"
      ) {
        rootComponent.set(
          project
            .configurations
            .getByName("${variant.name}RuntimeClasspath")
            .incoming
            .resolutionResult
            .rootComponent
        )

        outputDirectory.set(
          layout.buildDirectory.dir(
            "generated/dependency-attribution/${variant.name}/assets"
          )
        )

        outputFilename.set("dependency_attribution.proto")
      }

      @Suppress("UnstableApiUsage")
      variant.sources.assets?.addGeneratedSourceDirectory(
        task,
        DependencyAttributionTask::outputDirectory
      )
    }
  }
}
