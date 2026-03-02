package com.chvma.wordfight.audio

enum class BgmTrack(val fileName: String, val volume: Float) {
    Menu("main_music", 0.3f),
    Game("game_music", 0.2f),
}

interface BgmPlayer {
    fun startLoop(track: BgmTrack)
    fun pause()
    fun resume()
    fun stop()
}

expect fun createBgmPlayer(): BgmPlayer
