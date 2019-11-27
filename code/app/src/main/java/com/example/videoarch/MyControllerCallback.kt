package com.example.videoarch

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.TextView

class MyControllerCallback(var stateText: TextView, var titleText: TextView) :
    MediaControllerCompat.Callback() {

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

}