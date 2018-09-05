/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nestsuj.apps.reactivereducerdemo.utils

import android.content.Context
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.os.Build.VERSION.SDK_INT
import android.os.StatFs
import java.io.File

private const val MIN_DISK_CACHE_SIZE: Long = 5 * 1024 * 1024 // 5MB
private const val MAX_DISK_CACHE_SIZE: Long = 50 * 1024 * 1024 // 50MB

private const val HTTP_CACHE = "http-cache"

/**
 * Converted to Kotlin from Picasso Java source
 *
 * https://github.com/square/picasso/blob/master/picasso/src/main/java/com/squareup/picasso3/Utils.java
 */
fun createDefaultCacheDir(context: Context): File {
    val cache = File(context.applicationContext.cacheDir, HTTP_CACHE)
    if (!cache.exists()) {
        cache.mkdirs()
    }
    return cache
}

/**
 * Converted to Kotlin from Picasso Java source
 *
 * https://github.com/square/picasso/blob/master/picasso/src/main/java/com/squareup/picasso3/Utils.java
 */
fun calculateDiskCacheSize(dir: File): Long {
    var size = MIN_DISK_CACHE_SIZE

    try {
        val statFs = StatFs(dir.absolutePath)

        val blockCount = if (SDK_INT < JELLY_BEAN_MR2) statFs.blockCount.toLong() else statFs.blockCountLong

        val blockSize = if (SDK_INT < JELLY_BEAN_MR2) statFs.blockSize.toLong() else statFs.blockSizeLong
        val available = blockCount * blockSize
        // Target 2% of the total space.
        size = available / 50
    } catch (ignored: IllegalArgumentException) {}

    // Bound inside min/max size for disk cache.
    return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE)
}