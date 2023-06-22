package com.eygraber.dependency.attribution

import android.content.res.AssetManager

fun AssetManager.loadLibraryAttributionData() =
  Dependencies.ADAPTER.decode(
    open("dependency_attribution.proto")
  ).toAttributionData()
