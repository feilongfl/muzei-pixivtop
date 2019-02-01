/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.muzei.feilong.pixivtop

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.muzei.feilong.pixivtop.BuildConfig.PIXIV_TOP
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import java.io.IOException

class PixivWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "Pixiv"

        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance()
            workManager.enqueue(OneTimeWorkRequestBuilder<PixivWorker>()
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build())
        }
    }

    override fun doWork(): Result {
        val photos = try {
            PixivService.popularPhotos()
        } catch (e: IOException) {
            Log.w(TAG, "Error reading Pixiv response", e)
            return Result.retry()
        }

        if (photos.isEmpty()) {
            Log.w(TAG, "No photos returned from API.")
            return Result.failure()
        }

        val providerClient = ProviderContract.getProviderClient(
                applicationContext, PIXIV_TOP)
        val attributionString = applicationContext.getString(R.string.attribution)
        providerClient.addArtwork(photos.map { photo ->
            Artwork().apply {
                token = photo.id
                title = photo.title ?: attributionString
                byline = photo.artistname
                attribution = if (photo.title != null) attributionString else null
                persistentUri = photo.url.toUri()
                webUri = photo.url.toUri()
                metadata = photo.url
            }
        })
        return Result.success()
    }
}
