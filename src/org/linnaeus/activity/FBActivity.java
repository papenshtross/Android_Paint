package org.linnaeus.activity;

/*
 * Copyright 2009 Codecarpet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collections;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.codecarpet.fbconnect.FBDialog;
import com.codecarpet.fbconnect.FBFeedActivity;
import com.codecarpet.fbconnect.FBLoginButton;
import com.codecarpet.fbconnect.FBPermissionActivity;
import com.codecarpet.fbconnect.FBRequest;
import com.codecarpet.fbconnect.FBSession;
import com.codecarpet.fbconnect.FBDialog.FBDialogDelegate;
import com.codecarpet.fbconnect.FBLoginButton.FBLoginButtonStyle;
import com.codecarpet.fbconnect.FBRequest.FBRequestDelegate;
import com.codecarpet.fbconnect.FBSession.FBSessionDelegate;


public class FBActivity extends Activity {

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // This application will not work until you enter your Facebook application's API key here:

    private static final String kApiKey = "d8fe2132dcfedd87cd35ea7aaab80171";

    // Enter either your API secret or a callback URL (as described in documentation):
    private static final String kApiSecret = "2677187cbc202cd1eb133e4f635830fe";
    private static final String kGetSessionProxy = null; // "<YOUR SESSION CALLBACK)>";
    private static final int PERMISSIONREQUESTCODE = 1;
    private static final int MESSAGEPUBLISHED = 2;
    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private FBSession _session;
    private FBLoginButton _loginButton;
    private TextView _label;
    private Button _permissionButton;
    private Button _feedButton;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        if (kGetSessionProxy != null) {
            _session = FBSession.getSessionForApplication_getSessionProxy(kApiKey, kGetSessionProxy, new FBSessionDelegateImpl());
        } else {
            _session = FBSession.getSessionForApplication_secret(kApiKey, kApiSecret, new FBSessionDelegateImpl());
        }

        setContentView(R.layout.facebook);

        _label = (TextView) findViewById(R.id.label);
        _permissionButton = (Button) findViewById(R.id.permissionButton);
        _permissionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				askPermission();
			}
		});

        _feedButton = (Button) findViewById(R.id.feedButton);
        _feedButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				publishFeed();
			}
		});

        _loginButton = (FBLoginButton) findViewById(R.id.login);
        _loginButton.setStyle(FBLoginButtonStyle.FBLoginButtonStyleWide);
        _loginButton.setSession(_session);

        _session.resume(this);

    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private void askPermission() {
        Intent intent = new Intent(this, FBPermissionActivity.class);
        intent.putExtra("permissions", new String[]{"publish_stream"});
        this.startActivityForResult(intent, PERMISSIONREQUESTCODE );
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode){
		case PERMISSIONREQUESTCODE:
			if (resultCode == 1)
				_permissionButton.setVisibility(View.INVISIBLE);
        default:
        	return;
		}

	}

	private void publishFeed() {
        Intent intent = new Intent(this, FBFeedActivity.class);
        intent.putExtra("userMessagePrompt", "Example prompt");
        intent.putExtra("attachment", "{\"name\":\"Facebook Connect for Android\",\"href\":\"http://code.google.com/p/fbconnect-android/\",\"caption\":\"Caption\",\"description\":\"Description\",\"media\":[{\"type\":\"image\",\"src\":\"http://img40.yfrog.com/img40/5914/iphoneconnectbtn.jpg\",\"href\":\"http://developers.facebook.com/connect.php?tab=iphone/\"}],\"properties\":{\"another link\":{\"text\":\"Facebook home page\",\"href\":\"http://www.facebook.com\"}}}");
        this.startActivityForResult(intent, MESSAGEPUBLISHED);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private class FBDialogDelegateImpl extends FBDialogDelegate {

        @Override
		public void didFailWithError(FBDialog dialog, Throwable error) {
            _label.setText(error.toString());
        }

    }

    private void checkPermission() {
    	String fql = "select publish_stream from permissions where uid == " + String.valueOf(_session.getUid());
		Map<String, String> params = Collections.singletonMap("query", fql);
		FBRequest.requestWithDelegate(new FBHasPermissionRD()).call("facebook.fql.query", params);
	}

    private class FBSessionDelegateImpl extends FBSessionDelegate {

        @Override
		public void session_didLogin(FBSession session, Long uid) {
            // we check if the user already has the permissions before displaying permission button
        	checkPermission();

        	mHandler.post(new Runnable() {
                public void run() {
                    _feedButton.setVisibility(View.VISIBLE);
                }
             });

            String fql = "select uid,name from user where uid == " + session.getUid();

            Map<String, String> params = Collections.singletonMap("query", fql);
            FBRequest.requestWithDelegate(new FBRequestDelegateImpl()).call("facebook.fql.query", params);
        }



		@Override
		public void sessionDidLogout(FBSession session) {
            mHandler.post(new Runnable() {
               public void run() {
                   _label.setText("");
                   _permissionButton.setVisibility(View.INVISIBLE);
                   _feedButton.setVisibility(View.INVISIBLE);
               }
            });
        }

    }

    private class FBRequestDelegateImpl extends FBRequestDelegate {

        @Override
		public void request_didLoad(FBRequest request, Object result) {

            String name = null;

            if (result instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) result;
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    name = jo.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            _label.setText("Logged in as " + name);
        }

        @Override
		public void request_didFailWithError(FBRequest request, Throwable error) {
            _label.setText(error.toString());
        }
    }

    private class FBHasPermissionRD extends FBRequestDelegate {

        @Override
		protected void request_didFailWithError(FBRequest request,
				Throwable error) {
			super.request_didFailWithError(request, error);
		}

		@Override
		public void request_didLoad(FBRequest request, Object result) {
            int hasPermission = 0;

            if (result instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) result;
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    hasPermission = jo.getInt("publish_stream");
                    if (hasPermission == 0)
                    {
                        mHandler.post(new Runnable() {
                            public void run() {
                                _permissionButton.setVisibility(View.VISIBLE);
                            }
                         });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
