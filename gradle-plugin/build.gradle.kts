plugins {
  `kotlin-dsl`
  id("com.eygraber.conventions-kotlin-library")
  id("com.eygraber.conventions-detekt")
  id("com.eygraber.conventions-publish-maven-central")
}

dependencies {
  compileOnly(libs.buildscript.android)
  compileOnly(libs.buildscript.kotlin)

  implementation(projects.model)
  implementation(libs.mavenModel)
}
