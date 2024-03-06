/*
Copyright (C) 2011-2020 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.maxmpz.poweramp.apiexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * This receiver is registered in manifest and can be used to trigger some processing based on Poweramp broadcast intents.<br>
 * Here we just dump received intent. Out MainActivity class receives and processes those intents via own receiver which is registered and unregistered as needed.
 */
public class APIReceiver extends BroadcastReceiver {
	private static final String TAG = "APIReceiver";

	private static final PlayingData playingData = new PlayingData();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        if (action == null) return;

        switch(action) {

            case PowerampAPI.ACTION_STATUS_CHANGED_EXPLICIT:
                MainActivity.debugDumpIntent(TAG, "ACTION_STATUS_CHANGED_EXPLICIT", intent);

				boolean isPlaying = intent.getBooleanExtra(PowerampAPI.EXTRA_PAUSED, false);
				playingData.playState = isPlaying ? 1 : 0;
				playingData.positionMs = intent.getIntExtra(PowerampAPI.Track.POSITION, 0) * 1000;

				PostData();
				break;

			case PowerampAPI.ACTION_TRACK_POS_SYNC:
				// mostly doesn't seem to work :( but here just in case it feels like it
				MainActivity.debugDumpIntent(TAG, "ACTION_TRACK_POS_SYNC", intent);
				playingData.positionMs = intent.getIntExtra(PowerampAPI.Track.POSITION, 0) * 1000;
				PostData();
                break;

            case PowerampAPI.ACTION_TRACK_CHANGED_EXPLICIT:
				TrackChanged(context, intent);
				PostData();

				MainActivity.debugDumpIntent(TAG, "ACTION_TRACK_CHANGED_EXPLICIT", intent);
                break;

            default:
                MainActivity.debugDumpIntent(TAG, "UNKNOWN", intent);
                break;
        }
    }

	private static void TrackChanged(Context context, Intent intent) {
		playingData.title = intent.getStringExtra(PowerampAPI.Track.TITLE);
		playingData.artist = intent.getStringExtra(PowerampAPI.Track.ARTIST);
		playingData.album = intent.getStringExtra(PowerampAPI.Track.ALBUM);
		playingData.durationMs = intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0);
		playingData.positionMs = intent.getIntExtra(PowerampAPI.Track.POSITION, 0) * 1000;

		// Get the album art
		Bundle track = intent.getBundleExtra(PowerampAPI.EXTRA_TRACK);
		Bitmap bitmap = PowerampAPIHelper.getAlbumArt(context, track, 200, 200);

		if (bitmap == null) {
			playingData.albumArt = "";
			return;
		}

		bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

		// album art to base64
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 30, byteArrayOutputStream);
		byte[] byteArray = byteArrayOutputStream.toByteArray();
        playingData.albumArt = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
	}

	private static void PostData() {
		String json = new Gson().toJson(playingData);

		// log
		Log.d(TAG, "AutumnPosting: " + json);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.execute(() -> {
			try {
				URL url = new URL("https://example.org/now-playing");
				URLConnection con = url.openConnection();
				HttpsURLConnection http = (HttpsURLConnection) con;
				http.setRequestMethod("POST");
				http.setDoOutput(true);

				// Add authentication header
				http.setRequestProperty("Authorization", "Basic [insert your basic auth here]");

				byte[] out = (json).getBytes();
				int length = out.length;

				http.setFixedLengthStreamingMode(length);
				http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				http.connect();
				http.getOutputStream().write(out);

				Log.d(TAG, "AutumnResponse: " + http.getResponseCode());

				http.disconnect();
			} catch (Exception e) {
				Log.w(TAG, "Failed to send now-playing " + e, e);
			}
		});
	}

	private static class PlayingData {
		String title;
		String artist;
		String album;
		int durationMs;
		int positionMs;
		String albumArt;
		int playState;
	}

}
