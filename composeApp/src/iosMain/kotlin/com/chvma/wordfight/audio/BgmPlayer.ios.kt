package com.chvma.wordfight.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle

class IosBgmPlayer : BgmPlayer {
    private var player: AVAudioPlayer? = null
    private var currentTrack: BgmTrack? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun startLoop(track: BgmTrack) {
        if (currentTrack == track) {
            player?.let { existing ->
                existing.volume = track.volume
                if (!existing.playing) {
                    existing.play()
                }
                return
            }
        }
        stop()
        val url = NSBundle.mainBundle.URLForResource(track.fileName, "mp3") ?: return
        val p = AVAudioPlayer(contentsOfURL = url, error = null) ?: return
        p.numberOfLoops = -1
        p.volume = track.volume
        p.prepareToPlay()
        p.play()
        player = p
        currentTrack = track
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        val p = player ?: return
        if (!p.playing) {
            p.play()
        }
    }

    override fun stop() {
        val p = player ?: return
        p.stop()
        player = null
        currentTrack = null
    }
}

actual fun createBgmPlayer(): BgmPlayer = IosBgmPlayer()
