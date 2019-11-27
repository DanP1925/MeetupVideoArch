package com.example.videoarch

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.TextView

class MyControllerCallback(var stateText: TextView, var titleText: TextView) :
    MediaControllerCompat.Callback() {

    override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(playbackState)
        stateText.text = when (playbackState?.state) {
            PlaybackStateCompat.STATE_PLAYING -> "Reproduciendo"
            PlaybackStateCompat.STATE_PAUSED -> "Pausado"
            PlaybackStateCompat.STATE_STOPPED -> "Detenido"
            else -> "Error"
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        super.onMetadataChanged(metadata)
        titleText.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
    }

}