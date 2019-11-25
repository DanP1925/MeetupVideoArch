package com.example.videoarch

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            setCallback(MySessionCallbacks())
        }

        MediaControllerCompat(this, mediaSession).also { mediaControllerCompat ->
            MediaControllerCompat.setMediaController(this, mediaControllerCompat)
        }
    }

    override fun onPause() {
        super.onPause()
        if (android.os.Build.VERSION.SDK_INT <= 23) {
            this.mediaController.transportControls.stop()
        }
    }

    override fun onStop() {
        super.onStop()
        if (android.os.Build.VERSION.SDK_INT > 23) {
            this.mediaController.transportControls.stop()
        }
    }

}
