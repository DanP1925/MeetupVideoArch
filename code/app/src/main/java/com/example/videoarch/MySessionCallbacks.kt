package com.example.videoarch

import android.media.MediaPlayer
import android.support.v4.media.session.MediaSessionCompat

class MySessionCallbacks(var player: MediaPlayer) : MediaSessionCompat.Callback() {

    override fun onPlay() {
        super.onPlay()
        player.start()
    }

    override fun onStop() {
        super.onPlay()
        player.stop()
    }

    override fun onPause() {
        super.onPlay()
        player.pause()
    }

}