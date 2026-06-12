package com.chvma.wordfight.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.chvma.pronounceWord.R
import com.chvma.wordfight.speech.appContext

/**
 * All methods are expected to run on the main thread; the heavy work
 * (decoder/file preparation) happens off-thread via [MediaPlayer.prepareAsync],
 * and playback starts from the prepared callback.
 */
class AndroidBgmPlayer : BgmPlayer {
    private var player: MediaPlayer? = null
    private var currentTrack: BgmTrack? = null
    private var isPrepared = false

    // Desired state, consulted by async callbacks: preparation finishes later
    // and audio focus can change at any moment.
    private var wantPlaying = false
    private var pausedByFocusLoss = false
    private var hasFocus = false

    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedByFocusLoss) {
                    pausedByFocusLoss = false
                    player?.takeIf { wantPlaying && isPrepared && !it.isPlaying }?.start()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                hasFocus = false
                pauseForFocusLoss()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseForFocusLoss()
            // CAN_DUCK is not delivered here: the framework ducks automatically
            // on API 26+ unless setWillPauseWhenDucked(true) is set.
        }
    }

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(audioAttributes)
        .setOnAudioFocusChangeListener(focusListener, Handler(Looper.getMainLooper()))
        .build()

    override fun startLoop(track: BgmTrack) {
        if (currentTrack == track) {
            val existing = player
            if (existing != null) {
                wantPlaying = true
                existing.setVolume(track.volume, track.volume)
                if (isPrepared && !existing.isPlaying && requestFocus()) {
                    existing.start()
                }
                return
            }
        }
        stop()
        currentTrack = track
        wantPlaying = true
        isPrepared = false

        val resId = when (track) {
            BgmTrack.Menu -> R.raw.main_music
            BgmTrack.Game -> R.raw.game_music
        }
        val p = MediaPlayer()
        player = p
        p.setAudioAttributes(audioAttributes)
        p.isLooping = true
        p.setVolume(track.volume, track.volume)
        p.setOnPreparedListener {
            // The track may have been switched while this player was still
            // preparing; isPrepared must describe the current player only.
            if (player != p) return@setOnPreparedListener
            isPrepared = true
            if (wantPlaying && requestFocus()) {
                p.start()
            }
        }
        p.setOnErrorListener { _, what, extra ->
            Log.w(TAG, "BGM playback error: what=$what extra=$extra")
            if (player == p) {
                player = null
                currentTrack = null
                isPrepared = false
                abandonFocus()
            }
            p.release()
            true
        }
        try {
            appContext.resources.openRawResourceFd(resId).use { afd ->
                p.setDataSource(afd)
            }
            p.prepareAsync()
        } catch (error: Exception) {
            Log.w(TAG, "Failed to prepare BGM track $track", error)
            p.release()
            player = null
            currentTrack = null
        }
    }

    override fun pause() {
        wantPlaying = false
        pausedByFocusLoss = false
        player?.takeIf { isPrepared && it.isPlaying }?.pause()
        abandonFocus()
    }

    override fun resume() {
        wantPlaying = true
        val p = player ?: return
        if (isPrepared && !p.isPlaying && requestFocus()) {
            p.start()
        }
    }

    override fun stop() {
        wantPlaying = false
        pausedByFocusLoss = false
        val p = player ?: return
        player = null
        currentTrack = null
        isPrepared = false
        try {
            p.stop()
        } catch (_: IllegalStateException) {
            // Not prepared yet or already stopped.
        }
        p.release()
        abandonFocus()
    }

    private fun pauseForFocusLoss() {
        val p = player ?: return
        if (isPrepared && p.isPlaying) {
            pausedByFocusLoss = true
            p.pause()
        }
    }

    private fun requestFocus(): Boolean {
        if (hasFocus) return true
        hasFocus = audioManager.requestAudioFocus(focusRequest) ==
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasFocus
    }

    private fun abandonFocus() {
        if (!hasFocus) return
        audioManager.abandonAudioFocusRequest(focusRequest)
        hasFocus = false
    }

    private companion object {
        const val TAG = "AndroidBgmPlayer"
    }
}

actual fun createBgmPlayer(): BgmPlayer = AndroidBgmPlayer()
