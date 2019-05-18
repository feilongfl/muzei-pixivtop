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

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

internal interface PixivService {

    companion object {
        private fun getDaysAgo(daysAgo: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            return calendar.time
        }

        private fun createService(): PixivService {
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        var request = chain.request()
                        val dateYesterday = SimpleDateFormat("yyyy-MM-dd").format(getDaysAgo(4))
                        val url = request.url().newBuilder()
                                .addQueryParameter("page", (0).toString())
                                .addQueryParameter("date", dateYesterday)
                                .addQueryParameter("mode", "day")
                                .build()
                        request = request.newBuilder().url(url)
                                .addHeader("Referer", "https://pixivic.com").build()
                        chain.proceed(request)
                    }
                    .build()

            val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.pixivic.com/")
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

            return retrofit.create()
        }

        @Throws(IOException::class)
        internal fun popularPhotos(): Photo {
            return createService().popularPhotos.execute().body()
                    ?: throw IOException("Response was null")
        }

        @Throws(IOException::class)
        internal fun trackDownload(photoURL: String) {
            createService().trackDownload(photoURL).execute()
        }
    }

    @get:GET("ranks")
    val popularPhotos: Call<Photo>

    @GET("{url}")
    fun trackDownload(@Path("url") photoURL: String): Call<Any>

}
