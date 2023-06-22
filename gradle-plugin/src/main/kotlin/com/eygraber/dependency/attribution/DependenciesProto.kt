package com.eygraber.dependency.attribution

import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.logging.Logger
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.FileReader

internal fun ResolvedComponentResult.serializeDependenciesProto(
  dependencyHandler: DependencyHandler,
  logger: Logger,
): Dependencies {
  val ids = findAllResolvedDependencies()

  val licenseUrls = mutableListOf<String>()
  val licenseNames = mutableListOf<String>()
  val developerNames = mutableListOf<String>()
  val scmUrls = mutableListOf<String>()

  val pomReader = MavenXpp3Reader()

  val dependencies =
    dependencyHandler
      .resolvedComponentsSequence(ids)
      .mapToIdAndPomFile(pomReader, logger)
      .map { (id, pom) ->
        val coordinates = id.displayName

        val licenseUrlAndNameIndices = pom.licenseUrlAndNameIndices(
          licenseUrls = licenseUrls,
          licenseNames = licenseNames
        )

        val developerNamesIndices = pom.developersIndices(
          developerNames = developerNames
        )

        val scmUrlIndex = pom.scmUrlIndex(
          scmUrls = scmUrls
        )

        val pomName = pom.name.normalize()
        val pomDescription = pom.description.normalize()

        Dependency(
          coordinates = coordinates,
          licenseIndices = licenseUrlAndNameIndices.map { (url, name) ->
            License(urlIndex = url, nameIndex = name)
          },
          developerNamesIndices = developerNamesIndices,
          scmUrlIndex = scmUrlIndex,
          pomName = pomName,
          pomDescription = pomDescription
        )
      }
      .toList()

  require(dependencies.isNotEmpty()) {
    "There was an error gathering dependencies for attribution; nothing found"
  }

  return Dependencies(
    licenseUrls = licenseUrls,
    licenseNames = licenseNames,
    developerNames = developerNames,
    scmUrls = scmUrls,
    dependencies = dependencies
  )
}

private fun DependencyHandler.resolvedComponentsSequence(ids: List<DefaultModuleComponentIdentifier>) =
  createArtifactResolutionQuery()
    .forComponents(ids)
    .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
    .execute()
    .resolvedComponents
    .asSequence()

private fun Sequence<ComponentArtifactsResult?>.mapToIdAndPomFile(
  pomReader: MavenXpp3Reader,
  logger: Logger
) = mapNotNull { component ->
  component?.id?.let { id ->
    val pomArtifact =
      component
        .getArtifacts(MavenPomArtifact::class.java)
        .firstOrNull { it is ResolvedArtifactResult }

    if(pomArtifact is ResolvedArtifactResult) {
      id to pomReader.read(FileReader(pomArtifact.file))
    }
    else {
      logger.warn("Couldn't find a resolved MavenPomArtifact for $id")
      null
    }
  }
}

private fun Model.licenseUrlAndNameIndices(
  licenseUrls: MutableList<String>,
  licenseNames: MutableList<String>
) = licenses.orEmpty().map { license ->
  val licenseUrl = license.normalizedUrl
  val licenseName = license.normalizedName(licenseUrl)

  val licenseUrlIndex =
    licenseUrls
      .indexOf(licenseUrl)
      .takeIf { it >= 0 }
      ?: licenseUrls.appendAndGetIndex(licenseUrl)

  val licenseNameIndex =
    licenseNames
      .indexOf(licenseName)
      .takeIf { it >= 0 }
      ?: licenseNames.appendAndGetIndex(licenseName)

  licenseUrlIndex to licenseNameIndex
}

private fun Model.developersIndices(
  developerNames: MutableList<String>
) = developers.orEmpty().map { developer ->
  val devName = developer.name.normalize()

  developerNames
    .indexOf(devName)
    .takeIf { it >= 0 }
    ?: developerNames.appendAndGetIndex(devName)
}

private fun Model.scmUrlIndex(
  scmUrls: MutableList<String>
) = when(val scmUrl = scm?.url.normalize()) {
  null -> -1
  else ->
    scmUrls
      .indexOf(scmUrl)
      .takeIf { it >= 0 }
      ?: scmUrls.appendAndGetIndex(scmUrl)
}

private fun MutableList<String>.appendAndGetIndex(
  element: String?
) = when(element) {
  null -> -1
  else -> {
    this += element
    lastIndex
  }
}

private val License.normalizedUrl: String?
  get() = when(val normalizedUrl = url.normalize()) {
    "https://www.apache.org/licenses/LICENSE-2.0",
    "http://www.apache.org/licenses/LICENSE-2.0.txt",
    "https://www.apache.org/licenses/LICENSE-2.0.txt",
    "https://api.github.com/licenses/apache-2.0",
    "https://spdx.org/licenses/Apache-2.0.html" -> "https://spdx.org/licenses/Apache-2.0"

    "https://opensource.org/licenses/mit-license.php",
    "https://opensource.org/licenses/mit-license",
    "https://opensource.org/licenses/MIT",
    "https://api.github.com/licenses/mit",
    "https://spdx.org/licenses/MIT.html" -> "https://spdx.org/licenses/MIT"

    else -> normalizedUrl
  }

private fun License.normalizedName(url: String?) = when(url) {
  null -> null
  "https://spdx.org/licenses/Apache-2.0" -> "Apache License 2.0"
  "https://spdx.org/licenses/MIT" -> "MIT License"
  else -> name.normalize()
}

private fun String?.normalize() = when {
  isNullOrBlank() -> null
  this == "null" -> null
  else -> replace("\"", "\\\"").replace("\n", "\\n").trim()
}
