package com.example.videoarch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Button
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private lateinit var videoView: VideoView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
    private var hasFocus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_STORAGE
            )
        } else {
            setupVideoView()
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setupPlayButton()
        setupPauseButton()
        setupStopButton()

    }

    private fun hasPermissionGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setupVideoView()
                }
                return
            }
        }
    }

    private fun setupVideoView() {
        videoView = findViewById(R.id.video)
        videoView.setVideoPath(MEDIA_SOURCE)
        videoView.setOnPreparedListener {
            mediaSession = MediaSessionCompat(this, "Session_Tag").apply {

                setMediaButtonReceiver(null)

                val stateBuilder = PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_STOP
                    )
                setPlaybackState(stateBuilder.build())

                setCallback(MySessionCallbacks(it, MEDIA_SOURCE))
            }

            MediaControllerCompat(this, mediaSession).also {
                it.registerCallback(
                    MyControllerCallback(
                        findViewById(R.id.state), findViewById(R.id.title)
                    )
                )
                MediaControllerCompat.setMediaController(this, it)
            }
        }
    }

    private fun setupPlayButton() {
        playButton = findViewById(R.id.play)
        playButton.setOnClickListener {
            var result = -1
            if (!hasFocus) {
                result = requestAudioFocus()
            }
            if (hasFocus || result == AudioManager.AUDIOFOCUS_GAIN) {
                mediaController.transportControls.play()
                mediaSession.setMetadata(
                    MediaMetadataCompat
                        .Builder()
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            "Electric Light Orchestra"
                        )
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Mr. Blue Sky")
                        .build()
                )
                mediaSession.setPlaybackState(
                    PlaybackStateCompat
                        .Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1.0F
                        ).build()
                )
            }
        }
    }

    private fun setupPauseButton() {
        pauseButton = findViewById(R.id.pause)
        pauseButton.setOnClickListener {
            mediaController.transportControls.pause()
            mediaSession.setPlaybackState(
                PlaybackStateCompat
                    .Builder()
                    .setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1.0F
                    ).build()
            )
        }
    }

    private fun setupStopButton() {
        stopButton = findViewById(R.id.stop)
        stopButton.setOnClickListener {
            if (hasFocus) {
                abandonAudioFocus()
            }
            mediaController.transportControls.stop()
            mediaSession.setPlaybackState(
                PlaybackStateCompat
                    .Builder()
                    .setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1.0F
                    ).build()
            )
        }
    }

    private fun requestAudioFocus(): Int {
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        }

        return if (Build.VERSION.SDK_INT >= 26) {
            requestAudioFocusFromOreo()
        } else {
            requestAudioFocusUntilOreo()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusFromOreo() = audioManager.requestAudioFocus(
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener(audioFocusChangeListener, Handler())
            build()
        }
    )


    private fun requestAudioFocusUntilOreo() = audioManager.requestAudioFocus(
        audioFocusChangeListener,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN
    )


    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            this.mediaController.transportControls.stop()
            abandonAudioFocus()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            this.mediaController.transportControls.stop()
            abandonAudioFocus()
        }

    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            abandonAudioFocusFromOreo()
        } else {
            abandonAudioFocusUntilOreo()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocusFromOreo() = audioManager.abandonAudioFocusRequest(
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS).run {
            setOnAudioFocusChangeListener(audioFocusChangeListener, Handler())
            build()
        }
    )


    private fun abandonAudioFocusUntilOreo() =
        audioManager.abandonAudioFocus(audioFocusChangeListener)

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_STORAGE = 25
        const val MEDIA_SOURCE =
            "/storage/emulated/0/Movies/Guardianes_de_La_Galaxia_Vol2__Baby_Groot_Bailando.mp4"
    }


}
