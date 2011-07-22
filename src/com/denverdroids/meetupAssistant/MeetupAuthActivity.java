package com.denverdroids.meetupAssistant;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MeetupAuthActivity extends Activity {

	private final String TAG = getClass().getName();
	
	// Meetup OAuth
	//     Endpoints
	public static final String AUTH_URL = "https://secure.meetup.com/oauth2/authorize";
	public static final String TOKEN_URL = "https://secure.meetup.com/oauth2/access";
	
	//     Consumer
	public static final String REDIRECT_URI_SCHEME = "oauthresponse";
	public static final String REDIRECT_URI_HOST = "com.denverdroids.meetupassistant";
	public static final String REDIRECT_URI_HOST_APP = "meetupassistant";
	public static final String REDIRECT_URI = REDIRECT_URI_SCHEME + "://" + REDIRECT_URI_HOST + "/";
	public static final String CONSUMER_KEY = "geoagqchcb8vrpiu7o5rm6834d";
	public static final String CONSUMER_SECRET = "srq66sigt89ljncgltb8eltm23";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meetup_oauth);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		// get the redirect URI
		Uri uri = intent.getData();

		// if the redirect URI is the correct one, kick off the AsyncTask to
		// retrieve the access token. Otherwise, abort the auth process and 
		// return to this activity's auth UI.
		//
		if (uri != null) {
			String uriString = uri.toString();
			if (uriString.startsWith(REDIRECT_URI_SCHEME) &&
					uriString.contains(REDIRECT_URI_HOST_APP)) {
				new MeetupRetrieveAccessTokenTask().execute(uri);
				finish();
			}
		}
	}
	
	// TODO: Had to add rule to proguard.cfg so this method was not eliminated. Why is
	//	     Proguard even enabled, as it is not configured in default.properties??
	public void meetupAuthorizeOnClick(View v) {
		Log.i(TAG, "meetupAuthorizeOnClick");
		doMeetupAuthorizationRequest();
//		new MeetupRequestAuthorizationTask().execute();
	}
	
	private void doMeetupAuthorizationRequest() {
		OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest.authorizationLocation(
					AUTH_URL).setClientId(
					CONSUMER_KEY).setRedirectURI(
					REDIRECT_URI).buildQueryMessage();
		} catch (OAuthSystemException e) {
			Log.w(TAG, "OAuth request failed", e);
		}

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request
				.getLocationUri() + "&response_type=code&set_mobile=on"));
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | 
				Intent.FLAG_ACTIVITY_NO_HISTORY | 
				Intent.FLAG_FROM_BACKGROUND);
		MeetupAuthActivity.this.startActivity(intent);
	}

	private class MeetupRetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private final String TAG = getClass().getName();

		@Override
		protected Void doInBackground(Uri... params) {

			Uri uri = params[0];
			String code = uri.getQueryParameter("code");
			Log.i(TAG, code);

			OAuthClientRequest request = null;
			
			try {
				request = OAuthClientRequest.tokenLocation(TOKEN_URL)
						.setGrantType(GrantType.AUTHORIZATION_CODE).setClientId(
								CONSUMER_KEY).setClientSecret(
								CONSUMER_SECRET).setRedirectURI(
								REDIRECT_URI).setCode(code)
						.buildBodyMessage();
				
				OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

				OAuthAccessTokenResponse response = oAuthClient.accessToken(request);
				
				storeToPrefs("oAuthPrefs", "accessToken", response.getAccessToken());
				storeToPrefs("oAuthPrefs", "accessTokenExpiresIn", response.getExpiresIn());
				storeToPrefs("oAuthPrefs", "refreshToken", response.getRefreshToken());

			} catch (OAuthSystemException e) {
				Log.e(TAG, "OAuth System Exception - Couldn't get access token: " + e.toString());
			} catch (OAuthProblemException e) {
				Log.e(TAG, "OAuth Problem Exception - Couldn't get access token");
			}

			MeetupAuthActivity.this.startActivity(new Intent(MeetupAuthActivity.this, MainActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return null;
		}
		

		private void storeToPrefs(String namespace, String key, String value) {
			SharedPreferences prefs = getSharedPreferences(namespace, MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putString(key, value);
			edit.commit();
		}
		
		

	}
}
