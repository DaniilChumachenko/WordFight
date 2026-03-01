package com.chvma.wordfight.audio

import android.media.MediaPlayer
import com.chvma.wordfight.R
import com.chvma.wordfight.speech.appContext

class AndroidBgmPlayer : BgmPlayer {
    private var player: MediaPlayer? = null
    private var currentTrack: BgmTrack? = null

    override fun startLoop(track: BgmTrack) {
        if (player?.isPlaying == true && currentTrack == track) return
        stop()
        val resId = when (track) {
            BgmTrack.Menu -> R.raw.main_music
            BgmTrack.Game -> R.raw.game_music
        }
        val p = MediaPlayer.create(appContext, resId) ?: return
        p.isLooping = true
        p.setVolume(track.volume, track.volume)
        p.start()
        player = p
        currentTrack = track
    }

    override fun stop() {
        val p = player ?: return
        try {
            p.stop()
        } catch (_: IllegalStateException) {
            // Ignore if already stopped
        }
        p.release()
        player = null
        currentTrack = null
    }
}

actual fun createBgmPlayer(): BgmPlayer = AndroidBgmPlayer()
