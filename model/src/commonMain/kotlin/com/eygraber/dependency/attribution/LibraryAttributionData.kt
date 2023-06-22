package com.eygraber.dependency.attribution

data class LibraryAttributionLicense(
  val url: String?,
  val name: String?
)

data class LibraryAttributionData(
  val id: String,
  val licenses: List<LibraryAttributionLicense>,
  val scmUrl: String?,
  val developerNames: List<String>,
  val pomName: String?,
  val pomDescription: String?
)

internal fun Dependencies.toAttributionData(): List<LibraryAttributionData> {
  val licenseNamesLookup =
    licenseNames
      .mapIndexed { index, s ->
        index to s
      }
      .toMap()

  val licenseUrlsLookup =
    licenseUrls
      .mapIndexed { index, s ->
        index to s
      }
      .toMap()

  val scmUrlsLookup =
    scmUrls
      .mapIndexed { index, s ->
        index to s
      }
      .toMap()

  val developerNamesLookup =
    developerNames
      .mapIndexed { index, s ->
        index to s
      }
      .toMap()

  return dependencies.map { dependency ->
    with(dependency) {
      val licenses = licenseIndices.mapNotNull { license ->
        val url = licenseUrlsLookup[license.urlIndex]
        val name = licenseNamesLookup[license.nameIndex]

        if(name == null && url == null) {
          null
        }
        else {
          LibraryAttributionLicense(
            url = url,
            name = name
          )
        }
      }

      LibraryAttributionData(
        id = coordinates,
        licenses = licenses,
        scmUrl = scmUrlsLookup[scmUrlIndex],
        developerNames = developerNamesIndices.mapNotNull { developerNamesLookup[it] },
        pomName = pomName,
        pomDescription = pomDescription
      )
    }
  }
}
