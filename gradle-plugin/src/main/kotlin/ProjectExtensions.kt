@file:Suppress("NOTHING_TO_INLINE")

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.Project

internal inline fun Project.android(action: Action<BaseExtension>) {
  action.execute(extensions.getByType(BaseExtension::class.java))
}

internal inline fun Project.androidLibraryComponents(action: Action<LibraryAndroidComponentsExtension>) {
  action.execute(extensions.getByType(LibraryAndroidComponentsExtension::class.java))
}

internal inline fun Project.androidLibrary(action: Action<LibraryExtension>) {
  action.execute(extensions.getByType(LibraryExtension::class.java))
}
