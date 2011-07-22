package com.denverdroids.meetupAssistant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	private final String TAG = getClass().getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()!!");

		SharedPreferences prefs = getSharedPreferences("oAuthPrefs", MODE_PRIVATE);
		String token = prefs.getString("accessToken", null);

		if (token == null) {
			Intent intent = new Intent(this, MeetupAuthActivity.class);
			startActivity(intent);
		} else {
			setContentView(R.layout.main);
		}

	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
	}

}