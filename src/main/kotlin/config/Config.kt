package com.ehedgehog.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val token: String,
    val testToken: String,
    val client: HttpClientConfig? = null
)