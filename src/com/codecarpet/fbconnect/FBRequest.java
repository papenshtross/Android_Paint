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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import temporary.CcUtil;
import android.graphics.Bitmap;

public class FBRequest {

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // global

    static String kAPIVersion = "1.0";
    static String kAPIFormat = "JSON";
    static String kUserAgent = "FacebookConnect";
    static String kStringBoundary = "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";
    static String kEncoding = "UTF-8";

    static final long kTimeoutInterval = 180;

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private FBSession _session;
    private FBRequestDelegate _delegate;
    private String _url;
    private String _method;
    private Object _userInfo;
    private Map<String, String> _params;
    private Object _data;
    private Date _timestamp;
    private HttpURLConnection _connection;
    private StringBuilder _responseText;

    private FBRequest() {
    }

    public FBRequestDelegate getDelegate() {
        return _delegate;
    }

    /**
     * The URL which will be contacted to execute the request.
     */
    public String getUrl() {
        return _url;
    }

    /**
     * The API method which will be called.
     */
    public String getMethod() {
        return _method;
    }

    /**
     * An object used by the user of the request to help identify the meaning of the request.
     */
    public Object getUserInfo() {
        return _userInfo;
    }

    /**
     * The dictionary of parameters to pass to the method.
     * 
     * These values in the dictionary will be converted to strings using the standard Objective-C object-to-string
     * conversion facilities.
     */
    public Map<String, String> getParams() {
        return _params;
    }

    /**
     * The timestamp of when the request was sent to the server.
     */
    public Date getTimestamp() {
        return _timestamp;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // class public

    /**
     * Creates a new API request for the global session.
     */
    public static FBRequest request() {
        return requestWithSession(FBSession.getSession());
    }

    /**
     * Creates a new API request for the global session with a delegate.
     */
    public static FBRequest requestWithDelegate(FBRequestDelegate delegate) {
        return requestWithSession(FBSession.getSession(), delegate);
    }

    /**
     * Creates a new API request for a particular session.
     */
    public static FBRequest requestWithSession(FBSession session) {
        return new FBRequest().initWithSession(session);
    }

    /**
     * Creates a new API request for the global session with a delegate.
     */
    public static FBRequest requestWithSession(FBSession session, FBRequestDelegate delegate) {
        FBRequest request = requestWithSession(session);
        request._delegate = delegate;
        return request;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // private

    private String md5HexDigest(String input) {

        // TODO MD5

        return CcUtil.generateMD5(input);

        // // const char* str = [input UTF8String];
        // char[] result = new char[/* CC_MD5_DIGEST_LENGTH */0];
        // // CC_MD5(str, strlen(str), result);
        //
        // return String.format("%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x", result[0],
        // result[1], result[2], result[3], result[4], result[5], result[6], result[7], result[8], result[9],
        // result[10], result[11], result[12], result[13], result[14], result[15]);
    }

    private boolean isSpecialMethod() {
        return _method.equals("facebook.auth.getSession") || _method.equals("facebook.auth.createToken");
    }

    private String urlForMethod(String method) {
        return _session.getApiURL();
    }

    private String generateGetURL() {
        try {
            URL parsedURL = new URL(_url);
            String queryPrefix = parsedURL.getPath().contains("?") ? "&" : "?";

            List<String> pairs = new ArrayList<String>();
            for (Entry<String, String> entry : _params.entrySet()) {
                pairs.add(entry.getKey() + "=" + entry.getValue());
            }
            String params = CcUtil.componentsJoinedByString(pairs, "&");

            return _url + queryPrefix + params;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateCallId() {
        return String.format(Long.toString(System.currentTimeMillis()));
    }

    private String generateSig() {
        StringBuilder joined = new StringBuilder();

        List<String> keys = new ArrayList<String>(_params.keySet());
        Collections.sort(keys, CcUtil.CASE_INSENSITIVE_COMPARATOR);
        for (String obj : keys) {
            joined.append(obj);
            joined.append("=");
            Object value = _params.get(obj);
            if (value instanceof String) {
                joined.append(value);
            }
        }

        if (isSpecialMethod()) {
            if (_session.getApiSecret() != null) {
                joined.append(_session.getApiSecret());
            }
        } else if (_session.getSessionSecret() != null) {
            joined.append(_session.getSessionSecret());
        } else if (_session.getApiSecret() != null) {
            joined.append(_session.getApiSecret());
        }

        return md5HexDigest(joined.toString());
    }

    private byte[] generatePostBody() throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        String bodyString = "--" + kStringBoundary + "\r\n";
        String endLine = "\r\n--" + kStringBoundary + "\r\n";
  
        os.write(bodyString.getBytes(kEncoding));
        
        // write all string parameters from the parameter map
        for (Entry<String, String> entry : _params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            String cd = "Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n";
            
            os.write(cd.getBytes(kEncoding));
            os.write(value.getBytes(kEncoding));
            os.write(endLine.getBytes(kEncoding));
        }
  
        // write a bitmap value, if one exists
        if (_data != null) {
            if (_data instanceof Bitmap) {
                String cd = "Content-Disposition: form-data; filename=\"photo\"\r\n";
                String ct = "Content-Type: image/png\r\n\r\n";
                Bitmap image = (Bitmap)_data;
  
                os.write(cd.getBytes(kEncoding));
                os.write(ct.getBytes(kEncoding));
                image.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.write(endLine.getBytes(kEncoding));
                
            } else if (_data instanceof byte[]) {
                String cd = "Content-Disposition: form-data; filename=\"data\"\r\n";
                String ct = "Content-Type: content/unknown\r\n\r\n";
                byte[] data = (byte[])_data;
  
                os.write(cd.getBytes(kEncoding));
                os.write(ct.getBytes(kEncoding));
                os.write(data);
                os.write(endLine.getBytes(kEncoding));
            }
        }
        return os.toByteArray();
    }

    private Object parseJSONResponse(String data) throws JSONException {

        // TODO find some reliable way of creating appropriate JSON API class
        if (data.startsWith("[")) {
            return new JSONArray(data);
        } else {
            return new JSONObject(data);
        }
    }

    private void succeedWithResult(Object result) {
        if (_delegate != null) {
            _delegate.request_didLoad(this, result);
        }
    }
    
    private void failWithError(Throwable error) {
        if (_delegate != null) {
            _delegate.request_didFailWithError(this, error);
        }
    }

    private void handleResponseData(String data) {
        // FBLOG2(@"DATA: %s", data.bytes);
        try {
            Object result = parseJSONResponse(data);
  
            // check whether the result is an error
            if (result instanceof JSONObject) {
                JSONObject jso = (JSONObject)result;
                if (jso.has("error_code")) {
                    int errorCode = jso.getInt("error_code");
                    String errorMessage = jso.getString("error_msg");
                    JSONArray args = jso.getJSONArray("request_args");
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < args.length(); i++) {
                        JSONObject arg = args.getJSONObject(i);
                        map.put(arg.getString("key"), arg.getString("value"));
                    }
                    failWithError(new FBRequestError(errorCode, errorMessage, map));
                    return;
                }
            }
            
            // not an error, so call delegate
            succeedWithResult(result);
            
        } catch (JSONException e) {
            failWithError(e);
        }
    }

    public void connect() throws IOException {
        // FBLOG(@"Connecting to %@ s%@", _url, _params);
        _delegate.requestLoading(this);

        String url = (_method != null ? _url : generateGetURL());

        if (!url.endsWith("/")) {
            url += "/";
        }

        URL serverUrl = new URL(url);
        OutputStream out = null;
        InputStream in = null;
        try {
            _connection = (HttpURLConnection) serverUrl.openConnection();
            _connection.setRequestProperty("User-Agent", kUserAgent);

            byte[] body = null;
            if (_method != null) {
                _connection.setRequestMethod("POST");

                String contentType = "multipart/form-data; boundary=" + kStringBoundary;
                _connection.setRequestProperty("Content-Type", contentType);

                body = generatePostBody();
            }

            _connection.setDoOutput(true);
            _connection.connect();
            if (body != null) {
                out = _connection.getOutputStream();
                out.write(body);
            }

            in = _connection.getInputStream();
            _responseText = CcUtil.getResponse(in);
            // String prettyResponse = new JSONArray(response).toString(2);
            // Log.d(LOG, "Query result: " + prettyResponse);
        } finally {
            CcUtil.close(in);
            CcUtil.close(out);
            CcUtil.disconnect(_connection);
        }

        connectionDidFinishLoading();

        _timestamp = new Date();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // NSObject

    /**
     * Creates a new request paired to a session.
     */
    FBRequest initWithSession(FBSession session) {
        _session = session;
        _delegate = null;
        _url = null;
        _method = null;
        _params = null;
        _userInfo = null;
        _timestamp = null;
        _connection = null;
        _responseText = null;
        return this;
    }

    public String toString() {
        return "<FBRequest " + (_method != null ? _method : _url) + ">";
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // NSURLConnectionDelegate

    // private void connection_didReceiveResponse(String response) {
    // _responseText = new StringBuilder();
    //
    // _delegate.didReceiveResponse(response);
    // }
    //
    // private void connection_didReceiveData(String data) {
    // _responseText.append(data);
    // }

    private void connectionDidFinishLoading() {
        handleResponseData(_responseText.toString());

        _responseText = null;
        _connection = null;
    }

    // private void connection_didFailWithError(HttpURLConnection connection, NSError error) {
    // failWithError(error);
    //
    // _responseText = null;
    // _connection = null;
    // }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // public

    /**
     * Indicates if the request has been sent and is awaiting a response.
     */
    public boolean loading() {
        return _connection != null;
    }

    /**
     * Calls a method on the server asynchronously.
     * 
     * Use this form for API calls with no data parameter.
     * The delegate will be called for each stage of the loading process.
     */
    public void call(String method, Map<String, String> params) {
        callWithAnyData(method, params, null);
    }

    /**
     * Calls a method on the server asynchronously.
     * 
     * This version include an arbitrary byte array of data.
     * The delegate will be called for each stage of the loading process.
     */
    public void call(String method, Map<String, String> params, byte[] data) {
        callWithAnyData(method, params, data);
    }
    
    /**
     * Calls a method on the server asynchronously.
     * 
     * Include a Bitmap as a data parameter for photo uploads.
     * The delegate will be called for each stage of the loading process.
     */
    public void call(String method, Map<String, String> params, Bitmap data) {
        callWithAnyData(method, params, data);
    }
    
    /**
     * Calls a method on the server asynchronously.
     * 
     * The delegate will be called for each stage of the loading process.
     */
    private void callWithAnyData(String method, Map<String, String> params, Object data) {
        _url = urlForMethod(method);
        _method = method;
        _params = params != null ? new HashMap<String, String>(params) : new HashMap<String, String>();
        _data = data;

        _params.put("method", _method);
        _params.put("api_key", _session.getApiKey());
        _params.put("v", kAPIVersion);
        _params.put("format", kAPIFormat);

        if (!isSpecialMethod()) {
            _params.put("session_key", _session.getSessionKey());
            _params.put("call_id", generateCallId());

            if (_session.getSessionSecret() != null) {
                _params.put("ss", "1");
            }
        }

        _params.put("sig", generateSig());

        _session.send(this);
    }

    /**
     * Calls a URL on the server asynchronously.
     * 
     * The delegate will be called for each stage of the loading process.
     */
    public void post(String url, Map<String, String> params) {
        _url = url;
        _params = params != null ? new HashMap<String, String>(params) : new HashMap<String, String>();

        _session.send(this);
    }

    /**
     * Stops an active request before the response has returned.
     */
    public void cancel() {
        if (_connection != null) {
            // TODO
            // _connection.cancel();
            _connection = null;

            _delegate.requestWasCancelled(this);
        }
    }

    public static abstract class FBRequestDelegate {

        /**
         * Called just before the request is sent to the server.
         */
        protected void requestLoading(FBRequest request) {
        }

        // /**
        // * Called when the server responds and begins to send back data.
        // */
        // void request_didReceiveResponse(FBRequest request, NSURLResponse response);

        /**
         * Called when an error prevents the request from completing successfully.
         */
        protected void request_didFailWithError(FBRequest request, Throwable error) {
        }

        /**
         * Called when a request returns and its response has been parsed into an object.
         * 
         * The resulting object may be a dictionary, an array, a string, or a number, depending on thee format of the
         * API response.
         */
        protected void request_didLoad(FBRequest request, Object result) {
        }

        /**
         * Called when the request was cancelled.
         */
        protected void requestWasCancelled(FBRequest request) {
        }

    }

}
