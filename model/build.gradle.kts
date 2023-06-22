plugins {
  id("com.eygraber.conventions-kotlin-multiplatform")
  id("com.eygraber.conventions-android-library")
  id("com.eygraber.conventions-detekt")
  id("com.eygraber.conventions-publish-maven-central")
  alias(libs.plugins.wire)
}

android {
  namespace = "com.eygraber.dependency.attribution"
}

wire {
  kotlin {}
}

kotlin {
  kmpTargets(
    project = project,
    android = true,
    jvm = true
  )
}
