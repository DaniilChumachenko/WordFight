package com.chvma.wordfight

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform