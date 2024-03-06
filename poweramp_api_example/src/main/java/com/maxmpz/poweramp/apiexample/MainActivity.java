/*
Copyright (C) 2011-2023 Maksim Petrov

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.maxmpz.poweramp.player.PowerampAPI;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Set;

public class MainActivity extends AppCompatActivity
{
	public static void debugDumpIntent(@NonNull String tag, @NonNull String description, @Nullable Intent intent) {
		if(intent != null) {
			Log.w(tag, description + " debugDumpIntent action=" + intent.getAction() + " extras=" + dumpBundle(intent.getExtras()));
			Bundle track = intent.getBundleExtra(PowerampAPI.EXTRA_TRACK);
			if(track != null) {
				Log.w(tag, "track=" + dumpBundle(track));
			}
		} else {
			Log.e(tag, description + " debugDumpIntent intent is null");
		}
	}

	@SuppressWarnings("null")
	public static @NonNull String dumpBundle(@Nullable Bundle bundle) {
		if(bundle == null) {
			return "null bundle";
		}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = bundle.keySet();
		sb.append("\n");
		for(String key : keys) {
			sb.append('\t').append(key).append("=");
			Object val = bundle.get(key);
			sb.append(val);
			if(val != null) {
				sb.append(" ").append(val.getClass().getSimpleName());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}