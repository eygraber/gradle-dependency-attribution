package com.eygraber.dependency.attribution

import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

internal fun ResolvedComponentResult.findAllResolvedDependencies() =
  gatherDependenciesModuleComponentIds(
    component = this,
    seenComponents = mutableSetOf(),
    componentIds = mutableSetOf()
  ).distinctBy { id -> id.displayName }
    .sortedBy { id -> id.displayName }

private fun gatherDependenciesModuleComponentIds(
  component: ResolvedComponentResult,
  seenComponents: MutableSet<ResolvedComponentResult>,
  componentIds: MutableSet<DefaultModuleComponentIdentifier> = mutableSetOf()
): Set<DefaultModuleComponentIdentifier> {
  if(seenComponents.add(component)) {
    component
      .dependencies
      .filterIsInstance<ResolvedDependencyResult>()
      .forEach { dependency ->
        when(val selectedId = dependency.selected.id) {
          is DefaultModuleComponentIdentifier -> componentIds += selectedId
        }

        gatherDependenciesModuleComponentIds(dependency.selected, seenComponents, componentIds)
      }
  }

  return componentIds
}
