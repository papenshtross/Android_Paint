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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import temporary.CcDate;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * An FBSession represents a single user's authenticated session for a Facebook application.
 * 
 * To create a session, you must use the session key of your application (which can be found on the Facebook developer
 * website). You may then use the login dialog to ask the user to enter their email address and password. If successful,
 * you will get back a session key which can be used to make requests to the Facebook API.
 * 
 * Session keys are cached and stored on the disk of the device so that you do not need to ask the user to login every
 * time they launch the app. To restore the last active session, call the resume method after instantiating your
 * session.
 */
public class FBSession {

    private static final String PREFS_NAME = "FBSessionPreferences";
    
    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // global

    private static final String kAPIRestURL = "http://api.facebook.com/restserver.php";
    private static final String kAPIRestSecureURL = "https://api.facebook.com/restserver.php";

    private static final int kMaxBurstRequests = 3;
    private static final long kBurstDuration = 2; // NSTimeInterval - time interval in seconds

    private static FBSession sharedSession;

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private List<FBSessionDelegate> _delegates;
    private String _apiKey;
    private String _apiSecret;
    private String _getSessionProxy;
    private Long _uid;
    private String _sessionKey;
    private String _sessionSecret;
    private Date _expirationDate;
    private List<FBRequest> _requestQueue;
    private Date _lastRequestTime;
    private int _requestBurstCount;
    private Timer _requestTimer;

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // constructor

    private FBSession(String key, String secret, String getSessionProxy) {
        _delegates = new ArrayList<FBSessionDelegate>();//FBConnectGlobal.FBCreateNonRetainingArray();
        _apiKey = key;
        _apiSecret = secret;
        _getSessionProxy = getSessionProxy;
        _uid = Long.valueOf(0);
        _sessionKey = null;
        _sessionSecret = null;
        _expirationDate = null;
        _requestQueue = new ArrayList<FBRequest>();
        _lastRequestTime = new Date();
        _requestBurstCount = 0;
        _requestTimer = null;
    }

    /**
     * Constructs a session for an application.
     * 
     * @param secret
     *            the application secret (optional)
     * @param getSessionProxy
     *            a url to that proxies auth.getSession (optional)
     */
    private static FBSession initWithKey(String key, String secret, String getSessionProxy) {
        FBSession instance = new FBSession(key, secret, getSessionProxy);
        if (sharedSession == null) {
            sharedSession = instance;
        }

        return instance;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // class public

    /**
     * The globally shared session instance.
     */
    public static FBSession getSession() {
        return sharedSession;
    }

    /**
     * Sets the globally shared session instance.
     * 
     * This session is not retained, so you are still responsible for retaining it yourself. The first session that is
     * created is automatically stored here.
     */
    public static void setSession(FBSession session) {
        sharedSession = session;
    }

    /**
     * Constructs a session and stores it as the globally shared session instance.
     * 
     * @param secret
     *            the application secret (optional)
     */
    public static FBSession getSessionForApplication_secret(String key, String secret, FBSessionDelegate delegate) {
        FBSession session = initWithKey(key, /* secret */secret, /* getSessionProxy */null);
        
        session.getDelegates().add(delegate);
        return session;
    }

    /**
     * Constructs a session and stores it as the global singleton.
     * 
     * @param getSessionProxy
     *            a url to that proxies auth.getSession (optional)
     */
    public static FBSession getSessionForApplication_getSessionProxy(String key, String getSessionProxy, FBSessionDelegate delegate) {
        FBSession session = initWithKey(key, /* secret */null, /* getSessionProxy */getSessionProxy);
        session.getDelegates().add(delegate);
        return session;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // private

    public void save(Context context) {
        SharedPreferences defaults = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = defaults.edit();
        if (_uid != null) {
            editor.putLong("FBUserId", _uid);
        } else {
            editor.remove("FBUserId");
        }

        if (_sessionKey != null) {
            editor.putString("FBSessionKey", _sessionKey);
        } else {
            editor.remove("FBSessionKey");
        }

        if (_sessionSecret != null) {
            editor.putString("FBSessionSecret", _sessionSecret);
        } else {
            editor.remove("FBSessionSecret");
        }

        if (_expirationDate != null) {
            editor.putLong("FBSessionExpires", _expirationDate.getTime());
        } else {
            editor.remove("FBSessionExpires");
        }

        editor.commit();
    }

    public void unsave(Context context) {
        Editor defaults = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        
        defaults.remove("FBUserId");
        defaults.remove("FBSessionKey");
        defaults.remove("FBSessionSecret");
        defaults.remove("FBSessionExpires");
        defaults.commit();
    }

    private void startFlushTimer() {
        if (_requestTimer == null) {
            long timeIntervalSinceNow = CcDate.timeIntervalSinceNow(_lastRequestTime);
            long t = kBurstDuration + timeIntervalSinceNow;
            _requestTimer = new Timer();
            _requestTimer.schedule(requestTimerReady , t * 1000);
        }
    }

    private void enqueueRequest(FBRequest request) {
        _requestQueue.add(request);
        startFlushTimer();
    }

    private boolean performRequest(FBRequest request, boolean enqueue) {
        // Stagger requests that happen in short bursts to prevent the server from rejecting
        // them for making too many requests in a short time
    	long t = 0;
    	boolean burst = false;
    	if (_lastRequestTime != null)
    	{
    			t = new Date().getTime() - _lastRequestTime.getTime();
    			burst = t < kBurstDuration;
    	}
    	
        if (_lastRequestTime != null && burst && ++_requestBurstCount > kMaxBurstRequests) {
            if (enqueue) {
                enqueueRequest(request);
            }
            return false;
        } else {
            try {
                request.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!burst) {
                _requestBurstCount = 0;
                _lastRequestTime = request.getTimestamp();
            }
        }
        return true;
    }

    private void flushRequestQueue() {
        while (_requestQueue.size() > 0) {
            FBRequest request = _requestQueue.get(0);
            if (performRequest(request, false)) {
                _requestQueue.remove(0);
            } else {
                startFlushTimer();
                break;
            }
        }
    }

    private TimerTask requestTimerReady = new TimerTask() {
        public void run() {
            _requestTimer = null;
            flushRequestQueue();
        }
    };

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // public

    /**
     * The URL used for API HTTP requests.
     */
    public String getApiURL() {
        return kAPIRestURL;
    }

    /**
     * The URL used for secure API HTTP requests.
     */
    public String getApiSecureURL() {
        return kAPIRestSecureURL;
    }

    /**
     * Determines if the session is active and connected to a user.
     */
    public boolean isConnected() {
        return _sessionKey != null;
    }

    /**
     * Begins a session for a user with a given key and secret.
     */
    public void begin(Context context, Long uid, String sessionKey, String sessionSecret, Date expires) {
        _uid = uid;
        _sessionKey = sessionKey;
        _sessionSecret = sessionSecret;
        _expirationDate = (Date) expires.clone();
        save(context);
    }

    /**
     * Resumes a previous session whose uid, session key, and secret are cached on disk.
     */
    public boolean resume(Context context) {
        SharedPreferences defaults = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Long uid = defaults.getLong("FBUserId", 0);
        Log.d("FBSession", "FBUserId = " + uid);
        if (uid != 0) {
        	boolean loadSession = false;
        	long expires = defaults.getLong("FBSessionExpires", 0);
        	if (expires > 0) {
	            Date expirationDate = new Date(expires);
	            Log.d("FBSession", "expirationDate = " + expirationDate != null ? expirationDate.toLocaleString() : "null");
	            long timeIntervalSinceNow = CcDate.timeIntervalSinceNow(expirationDate);
	            Log.d("FBSession", "Time interval since now = " + timeIntervalSinceNow);
	            
	            if (expirationDate == null || timeIntervalSinceNow <= 0) {
	            	loadSession = true;
	            }
        	}
        	else {
        		Log.d("FBSession", "FBSessionExpires does not exist.  Loading session...");
        		loadSession = true;
        	}
            if (loadSession) {
                Log.d("FBSession", "Session can be loaded.  Loading...");
                _uid = uid;
                _sessionKey = defaults.getString("FBSessionKey", null);
                _sessionSecret = defaults.getString("FBSessionSecret", null);

                for (FBSessionDelegate delegate : _delegates) {
                    delegate.session_didLogin(this, uid);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Ends the current session and deletes the uid, session key, and secret from disk.
     */
    public void logout(Context context) {
        if (_sessionKey != null) {
            for (FBSessionDelegate delegate : _delegates) {
                delegate.session_willLogout(this, _uid);
            }

            // TODO Remove cookies that UIWebView may have stored

            // Remove cookies that UIWebView may have stored
            // NSHTTPCookieStorage* cookies = [NSHTTPCookieStorage sharedHTTPCookieStorage];
            // NSArray* facebookCookies = [cookies cookiesForURL:
            // [NSURL URLWithString:@"http://login.facebook.com"]];
            // for (NSHTTPCookie* cookie in facebookCookies) {
            // [cookies deleteCookie:cookie];
            // }

            _uid = Long.valueOf(0);
            _sessionKey = null;
            _sessionSecret = null;
            _expirationDate = null;
            unsave(context);

            for (FBSessionDelegate delegate : _delegates) {
                delegate.sessionDidLogout(this);
            }
        } else {
            unsave(context);
        }
    }

    /**
     * Sends a fully configured request to the server for execution.
     */
    public void send(FBRequest request) {
         performRequest(request, true);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // instance variables getters and setters

    /**
     * Delegates which implement FBSessionDelegate.
     */
    public List<FBSessionDelegate> getDelegates() {
        return _delegates;
    }

    /**
     * Your application's API key, as passed to the constructor.
     */
    public String getApiKey() {
        return _apiKey;
    }

    /**
     * Your application's API secret, as passed to the constructor.
     */
    public String getApiSecret() {
        return _apiSecret;
    }

    /**
     * The URL to call to create a session key after login.
     * 
     * This is an alternative to calling auth.getSession directly using the secret key.
     */
    public String getGetSessionProxy() {
        return _getSessionProxy;
    }

    /**
     * The current user's Facebook id.
     */
    public Long getUid() {
        return _uid;
    }

    /**
     * The current user's session key.
     */
    public String getSessionKey() {
        return _sessionKey;
    }

    /**
     * The current user's session secret.
     */
    public String getSessionSecret() {
        return _sessionSecret;
    }

    /**
     * The expiration date of the session key.
     */
    public Date getExpirationDate() {
        return _expirationDate;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    public static abstract class FBSessionDelegate {

        /**
         * Called when a user has successfully logged in and begun a session.
         */
        protected void session_didLogin(FBSession session, Long uid) {}

        /**
         * Called when a session is about to log out.
         */
        protected void session_willLogout(FBSession session, Long uid) {}

        /**
         * Called when a session has logged out.
         */
        protected void sessionDidLogout(FBSession session) {}

    }

}
