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

package com.codecarpet.fbconnect;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.codecarpet.fbconnect.FBRequest.FBRequestDelegate;

public class FBLoginDialog extends FBDialog {

    private static final String LOG = FBLoginDialog.class.getSimpleName();
    
    private static final String kLoginURL = "http://www.facebook.com/login.php";

    private FBRequest _getSessionRequest;
    private FBRequestDelegate _requestDelegate;

    public FBLoginDialog(Activity context, FBSession session) {
        super(context, session);
        _requestDelegate = new FBRequestDelegateImpl();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // private

    private void connectToGetSession(String token) {
        _getSessionRequest = FBRequest.requestWithSession(_session, _requestDelegate);
        Map<String, String> params = new HashMap<String, String>();
        params.put("auth_token", token);
        if (_session.getApiSecret() != null) {
            params.put("generate_session_secret", "1");
        }

        if (_session.getGetSessionProxy() != null) {
            _getSessionRequest.post(_session.getGetSessionProxy(), params);
        } else {
            _getSessionRequest.call("facebook.auth.getSession", params);
        }
    }

    private void loadLoginPage() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("fbconnect", "1");
        params.put("connect_display", "touch");
        params.put("api_key", _session.getApiKey());
        params.put("next", "fbconnect://success");

        try {
            loadURL(kLoginURL, "GET", params, null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // FBDialog

    @Override
    protected void load() {
        loadLoginPage();
    }

    @Override
    protected void dialogWillDisappear() {
        // _webView.stringByEvaluatingJavaScriptFromString("email.blur();");
        if (_getSessionRequest == null) {
            Log.w(LOG, "This should not be null, at least on iPhone it is not...");
        } else {
            _getSessionRequest.cancel();
        }
    }

    @Override
    protected void dialogDidSucceed(URI url) {
        String q = url.getQuery();
        int start = q.indexOf("auth_token=");
        if (start != -1) {
            int end = q.indexOf("&");
            int offset = start + "auth_token=".length();
            String token = end == -1 ? q.substring(offset) : q.substring(offset, end - offset);

            if (token != null) {
                connectToGetSession(token);
            }
        }
//        super.dialogDidSucceed(url);
    }

    private class FBRequestDelegateImpl extends FBRequestDelegate {

        @Override
        protected void request_didLoad(FBRequest request, Object result) {
            
            try {
                JSONObject jsonObject = (JSONObject) result;
                Long uid = jsonObject.getLong("uid"); // XXX maybe create Long?
                String sessionKey = jsonObject.getString("session_key");
                String sessionSecret = jsonObject.getString("secret");
                Long expires = jsonObject.getLong("expires");
                Date expiration = null;
                if (expires != null) {
                    expiration = new Date(expires);
                }
                            
                 _getSessionRequest = null;
                
                 _session.begin(mContext, uid, sessionKey, sessionSecret, expiration);
                 _session.resume(mContext);
                            
                 dismissWithSuccess(true, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        }

        @Override
        protected void request_didFailWithError(FBRequest request, Throwable error) {
            _getSessionRequest = null;

            dismissWithError(error, true);
        }

    }

}
