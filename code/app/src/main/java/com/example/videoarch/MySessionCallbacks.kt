package com.example.videoarch

import android.media.MediaPlayer
import android.support.v4.media.session.MediaSessionCompat

class MySessionCallbacks(var player: MediaPlayer, var filePath: String) :
    MediaSessionCompat.Callback() {

    private var stopped = false

    override fun onPlay() {
        super.onPlay()
        if (stopped) {
            player.prepare()
            stopped = false
        }
        player.start()
    }

    override fun onStop() {
        super.onPlay()
        player.stop()
        stopped = true
    }

    override fun onPause() {
        super.onPlay()
        player.pause()
    }

}