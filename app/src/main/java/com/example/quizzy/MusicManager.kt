package com.example.quizzy

import android.content.Context
import android.media.MediaPlayer

/**
 * Singleton object that manages background music and sound effects for the application.
 * It handles initialization, playback control, and persistent volume settings.
 */
object MusicManager {

    private var mediaPlayer: MediaPlayer? = null

    private const val PREFS_NAME = "quizzy_settings"
    private const val KEY_MUSIC_VOLUME = "music_volume"
    private const val KEY_SFX_VOLUME = "sfx_volume"
    
    /** Default volume level for background music. */
    private const val DEFAULT_MUSIC_VOLUME = 0.2f
    /** Default volume level for sound effects. */
    private const val DEFAULT_SFX_VOLUME = 0.7f

    /**
     * Initializes and starts the background music playback.
     * If music is already playing, it ensures it continues.
     * 
     * @param context The application context used to load resources and preferences.
     */
    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedVolume = prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)

            mediaPlayer = MediaPlayer.create(
                context.applicationContext,
                R.raw.background_music
            )

            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(savedVolume, savedVolume)
        }

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    /**
     * Pauses the background music if it is currently playing.
     */
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    /**
     * Resumes the background music if it is initialized and paused.
     */
    fun resumeMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    /**
     * Stops the background music and releases its resources.
     * The manager must call [startMusic] again to restart playback.
     */
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Updates the music volume level and persists it to shared preferences.
     * 
     * @param context The application context.
     * @param volume The new volume level (0.0 to 1.0).
     */
    fun setVolume(context: Context, volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)

        // Save volume setting
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, safeVolume).apply()

        // Apply volume change immediately to active playback
        mediaPlayer?.setVolume(safeVolume, safeVolume)
    }

    /**
     * Retrieves the persisted background music volume level.
     * 
     * @param context The application context.
     * @return The volume level as a Float.
     */
    fun getVolume(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
    }

    /**
     * Updates the sound effects volume level and persists it.
     * 
     * @param context The application context.
     * @param volume The new volume level (0.0 to 1.0).
     */
    fun setSFXVolume(context: Context, volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_SFX_VOLUME, safeVolume).apply()
    }

    /**
     * Retrieves the persisted sound effects volume level.
     * 
     * @param context The application context.
     * @return The volume level as a Float.
     */
    fun getSFXVolume(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME)
    }

    /**
     * Plays a standard UI click sound effect once.
     * 
     * @param context The application context.
     */
    fun playClickSound(context: Context) {
        val sound = MediaPlayer.create(context.applicationContext, R.raw.click_sound)

        val sfxVolume = getSFXVolume(context)
        sound.setVolume(sfxVolume, sfxVolume)

        sound.start()

        // Automatically release resources when the sound finishes playing
        sound.setOnCompletionListener {
            it.release()
        }
    }
}
