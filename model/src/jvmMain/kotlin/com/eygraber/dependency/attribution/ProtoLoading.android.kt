package com.eygraber.dependency.attribution

object LibraryAttributionDataLoader {
  fun invoke() {
    this::class
      .java
      .classLoader
      .getResourceAsStream("dependency_attribution.proto")
      ?.let { stream ->
        Dependencies
          .ADAPTER
          .decode(stream)
          .toAttributionData()
      }
      .orEmpty()
  }
}
