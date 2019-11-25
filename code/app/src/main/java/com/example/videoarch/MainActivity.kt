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
import android.widget.TextView
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
    private lateinit var stateText: TextView
    private lateinit var titleText: TextView
    private lateinit var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Pedir permiso
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_STORAGE
                )
            }
        } else {
            setupVideoView()
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        playButton = findViewById(R.id.play)
        playButton.setOnClickListener {
            val result = requestAudioFocus()
            mediaController.transportControls.play()
            mediaSession.setMetadata(
                MediaMetadataCompat
                    .Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Smash mouth")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "All star")
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
        stopButton = findViewById(R.id.stop)
        stopButton.setOnClickListener {
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

        stateText = findViewById(R.id.state)
        titleText = findViewById(R.id.title)
    }

    private fun requestAudioFocus(): Int {
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    mediaController.transportControls.stop()
                }
            }
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
        videoView.setVideoPath("/storage/emulated/0/Movies/Smash_Mouth_-_All_Star_Official_Music_Video.mp4")
        videoView.setOnPreparedListener {
            mediaSession = MediaSessionCompat(this, "Session_Tag").apply {

                //Por defecto se maneja tanto los media buttons como los transport controls
                //Tambien por defecto los convierte en transport control

                //Por ser video se le indica que en background no va a recibir ningun media button
                setMediaButtonReceiver(null)

                // Se le indica al controller que media buttons van a ser validos
                val stateBuilder = PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_STOP
                    )
                setPlaybackState(stateBuilder.build())

                // Se le asigna los callbacks para manejar el media session
                setCallback(MySessionCallbacks(it))
            }

            MediaControllerCompat(this, mediaSession).also { mediaControllerCompat ->
                mediaControllerCompat.registerCallback(object : MediaControllerCompat.Callback() {
                    override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                            stateText.text = "Reproduciendo"
                        } else {
                            stateText.text = "Pausado"
                        }
                    }

                    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                        super.onMetadataChanged(metadata)
                        titleText.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    }
                })
                MediaControllerCompat.setMediaController(this, mediaControllerCompat)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            this.mediaController.transportControls.stop()
            if (Build.VERSION.SDK_INT >= 26) {
                abandonAudioFocusFromOreo()
            } else {
                abandonAudioFocusUntilOreo()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            this.mediaController.transportControls.stop()
            if (Build.VERSION.SDK_INT >= 26) {
                abandonAudioFocusFromOreo()
            } else {
                abandonAudioFocusUntilOreo()
            }
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
    }


}
