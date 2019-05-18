package com.muzei.feilong.pixivtop

data class Photo(
        val `data`: Data,
        val message: String
)

data class Data(
        val date: String,
        val illustrations: List<Illustration>,
        val mode: String
)

data class Illustration(
        val caption: String,
        val create_date: String,
        val height: Int,
        val id: String,
        val meta_pages: List<MetaPage>,
        val meta_single_page: MetaSinglePage,
        val page_count: Int,
        val rank: Int,
        val sanity_level: Int,
        val tags: List<Tag>,
        val title: String,
        val tools: List<Any>,
        val type: String,
        val user: User,
        val width: Int
)

data class User(
        val account: String,
        val id: String,
        val name: String,
        val profile_image_urls: ProfileImageUrls
)

data class ProfileImageUrls(
        val medium: String
)

data class MetaSinglePage(
        val large_image_url: String?,
        val original_image_url: String?
)

data class Tag(
        val name: String
)

data class MetaPage(
        val image_urls: ImageUrls
)

data class ImageUrls(
        val large: String,
        val original: String
)