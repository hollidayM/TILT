package org.example.tilt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform