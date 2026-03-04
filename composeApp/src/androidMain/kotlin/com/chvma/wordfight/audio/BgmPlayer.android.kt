package com.chvma.wordfight.audio

import android.media.MediaPlayer
import com.chvma.pronounceWord.R
import com.chvma.wordfight.speech.appContext

class AndroidBgmPlayer : BgmPlayer {
    private var player: MediaPlayer? = null
    private var currentTrack: BgmTrack? = null

    override fun startLoop(track: BgmTrack) {
        if (currentTrack == track) {
            player?.let { existing ->
                existing.setVolume(track.volume, track.volume)
                if (!existing.isPlaying) {
                    existing.start()
                }
                return
            }
        }
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

    override fun pause() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
        }
    }

    override fun resume() {
        val p = player ?: return
        if (!p.isPlaying) {
            p.start()
        }
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
