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

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import java.io.IOException
import java.io.InputStream

class PixivArtProvider : MuzeiArtProvider() {

    companion object {
        private const val TAG = "Pixiv"

        private const val COMMAND_ID_VIEW_PROFILE = 1
        private const val COMMAND_ID_VISIT_PIXIV = 2
    }

    override fun onLoadRequested(initial: Boolean) {
        PixivWorker.enqueueLoad()
    }

    override fun getCommands(artwork: Artwork) = context?.run {
        listOf(
                UserCommand(COMMAND_ID_VIEW_PROFILE,
                        getString(R.string.action_view_profile, artwork.byline)),
                UserCommand(COMMAND_ID_VISIT_PIXIV,
                        getString(R.string.action_visit_unsplash)))
    } ?: super.getCommands(artwork)

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return
        when (id) {
            COMMAND_ID_VIEW_PROFILE -> {
                val profile = context.getString(R.string.unsplash_link) + artwork.metadata
                context.startActivity(Intent(Intent.ACTION_VIEW, profile?.toUri()))
            }
            COMMAND_ID_VISIT_PIXIV -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, artwork.webUri))
            }
        }
    }

    override fun openFile(artwork: Artwork): InputStream {
        return super.openFile(artwork).also {
            artwork.persistentUri?.run {
                try {
                    PixivService.trackDownload(this.toString())
                } catch (e: IOException) {
                    Log.w(TAG, "Error reporting download to Pixiv", e)
                }
            }
        }
    }
}
