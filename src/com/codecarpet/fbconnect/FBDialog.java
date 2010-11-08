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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import temporary.CcUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FBDialog extends FrameLayout {

    private static final String LOG = FBDialog.class.getSimpleName();

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // global

    private static final String kDefaultTitle = "Connect to Facebook";
    private static final String kStringBoundary = "3i2ndDfv2rTHiSisAbouNdArYfORhtTPEefj3q2f";

    private static final int kFacebookBlue = CcUtil.rgbFloatToInt(0.42578125f, 0.515625f, 0.703125f, 1.0f);
    private static final int kBorderGray = CcUtil.rgbFloatToInt(0.3f, 0.3f, 0.3f, 0.8f);
    private static final int kBorderBlack = CcUtil.rgbFloatToInt(0.3f, 0.3f, 0.3f, 1f);
    private static final int kBorderBlue = CcUtil.rgbFloatToInt(0.23f, 0.35f, 0.6f, 1.0f);

    private static final int kTransitionDuration = 200; // changed from original
    // 300ms

    private static final int kTitleMarginX = 8;
    private static final int kTitleMarginY = 4;
    private static final int kPadding = 10;
    private static final int kBorderWidth = 10;

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private FBDialogDelegate _delegate;
    protected FBSession _session;
    private URL _loadingURL;
    protected WebView _webView;
    private ProgressBar _spinner;
    private ImageView _iconView;
    private TextView _titleLabel;
    private ImageButton _closeButton;
    private int _orientation; // see ActivityInfo.SCREEN_ORIENTATION*
    private boolean _showingKeyboard;

    private LinearLayout content;
    private RelativeLayout mProgressWrapper;
    protected Activity mContext;

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    public FBSession getSession() {
        return _session;
    }

    public void setSession(FBSession session) {
        _session = session;
    }

    public FBDialogDelegate getDelegate() {
        return _delegate;
    }

    public void setDelegate(FBDialogDelegate delegate) {
        _delegate = delegate;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // private

    private void drawRect(Canvas context, Rect rect, int fillColor, float radius) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        paint.setAntiAlias(true);
        if (radius > 0) {
            context.drawRoundRect(new RectF(rect), radius, radius, paint);
        } else {
            context.drawRect(rect, paint);
        }
    }

    private void strokeLines(Canvas context, RectF rect, int strokeColor) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(strokeColor);
        paint.setStrokeWidth(1.0f);

        context.drawRect(0.5f, 0.5f, 0.5f, 0.5f, paint);
    }

    // - (BOOL)shouldRotateToOrientation:(UIDeviceOrientation)orientation {
    // if (orientation == _orientation) {
    // return NO;
    // } else {
    // return orientation == UIDeviceOrientationLandscapeLeft
    // || orientation == UIDeviceOrientationLandscapeRight
    // || orientation == UIDeviceOrientationPortrait
    // || orientation == UIDeviceOrientationPortraitUpsideDown;
    // }
    // }
    //
    // - (CGAffineTransform)transformForOrientation {
    // UIInterfaceOrientation orientation = [UIApplication
    // sharedApplication].statusBarOrientation;
    // if (orientation == UIInterfaceOrientationLandscapeLeft) {
    // return CGAffineTransformMakeRotation(M_PI*1.5);
    // } else if (orientation == UIInterfaceOrientationLandscapeRight) {
    // return CGAffineTransformMakeRotation(M_PI/2);
    // } else if (orientation == UIInterfaceOrientationPortraitUpsideDown) {
    // return CGAffineTransformMakeRotation(-M_PI);
    // } else {
    // return CGAffineTransformIdentity;
    // }
    // }
    //
    // - (void)sizeToFitOrientation:(BOOL)transform {
    // if (transform) {
    // self.transform = CGAffineTransformIdentity;
    // }
    //
    // CGRect frame = [UIScreen mainScreen].applicationFrame;
    // CGPoint center = CGPointMake(
    // frame.origin.x + ceil(frame.size.width/2),
    // frame.origin.y + ceil(frame.size.height/2));
    //   
    // CGFloat width = frame.size.width - kPadding * 2;
    // CGFloat height = frame.size.height - kPadding * 2;
    //   
    // _orientation = [UIApplication sharedApplication].statusBarOrientation;
    // if (UIInterfaceOrientationIsLandscape(_orientation)) {
    // self.frame = CGRectMake(kPadding, kPadding, height, width);
    // } else {
    // self.frame = CGRectMake(kPadding, kPadding, width, height);
    // }
    // self.center = center;
    //
    // if (transform) {
    // self.transform = [self transformForOrientation];
    // }
    // }
    //
    // - (void)updateWebOrientation {
    // UIInterfaceOrientation orientation = [UIApplication
    // sharedApplication].statusBarOrientation;
    // if (UIInterfaceOrientationIsLandscape(orientation)) {
    // [_webView stringByEvaluatingJavaScriptFromString:
    // @"document.body.setAttribute('orientation', 90);"];
    // } else {
    // [_webView stringByEvaluatingJavaScriptFromString:
    // @"document.body.removeAttribute('orientation');"];
    // }
    // }

    private void bounce1AnimationStopped() {
        ScaleAnimation scale = new ScaleAnimation(1.1f, 0.9f, 1.1f, 0.9f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(kTransitionDuration / 2);
        scale.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation arg0) {
                bounce2AnimationStopped();
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationStart(Animation arg0) {
            }
        });
        startAnimation(scale);
    }

    private void bounce2AnimationStopped() {
        ScaleAnimation scale = new ScaleAnimation(0.9f, 1, 0.9f, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(kTransitionDuration / 2);
        startAnimation(scale);
    }

    private URL generateURL(String baseURL, Map<String, String> params) throws MalformedURLException {

        StringBuilder sb = new StringBuilder(baseURL);
        Iterator<Entry<String, String>> it = params.entrySet().iterator();
        if (it.hasNext()) {
            sb.append('?');
        }
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(CcUtil.encode(entry.getValue()));
            if (it.hasNext()) {
                sb.append('&');
            }
        }
        return new URL(sb.toString());
    }

    private String generatePostBody(Map<String, String> params) {
        StringBuilder body = new StringBuilder();
        StringBuilder endLine = new StringBuilder("\r\n--").append(kStringBoundary).append("\r\n");

        body.append("--").append(kStringBoundary).append("\r\n");

        for (Entry<String, String> entry : params.entrySet()) {
            body.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
            String value = entry.getValue();
            if ("user_message_prompt".equals(entry.getKey())) {
            	body.append(value);
            }
            else {
                body.append(CcUtil.encode(value));
            }

            body.append(endLine);
        }

        return body.toString();
    }

    // - (void)addObservers {
    // [[NSNotificationCenter defaultCenter] addObserver:self
    // selector:@selector(deviceOrientationDidChange:)
    // name:@"UIDeviceOrientationDidChangeNotification" object:nil];
    // [[NSNotificationCenter defaultCenter] addObserver:self
    // selector:@selector(keyboardWillShow:)
    // name:@"UIKeyboardWillShowNotification" object:nil];
    // [[NSNotificationCenter defaultCenter] addObserver:self
    // selector:@selector(keyboardWillHide:)
    // name:@"UIKeyboardWillHideNotification" object:nil];
    // }
    //
    // - (void)removeObservers {
    // [[NSNotificationCenter defaultCenter] removeObserver:self
    // name:@"UIDeviceOrientationDidChangeNotification" object:nil];
    // [[NSNotificationCenter defaultCenter] removeObserver:self
    // name:@"UIKeyboardWillShowNotification" object:nil];
    // [[NSNotificationCenter defaultCenter] removeObserver:self
    // name:@"UIKeyboardWillHideNotification" object:nil];
    // }

    private void postDismissCleanup() {
        // [self removeObservers];
        // [self removeFromSuperview];

        mContext.finish();
    }

    private void dismiss(boolean animated) {
        dialogWillDisappear();
        _loadingURL = null;
        if (animated) {

            AlphaAnimation animation = new AlphaAnimation(1, 0);
            animation.setDuration(kTransitionDuration);

            postDismissCleanup();
            startAnimation(animation);
        } else {
            postDismissCleanup();
        }
    }

    private void cancel() {
        dismissWithSuccess(false, true);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // Object

    // public FBDialog(Context context) {
    // this(context, FBSession.getSession());
    // }

    public FBDialog(Activity context, FBSession session) {
        super(context);

        mContext = context;
        _delegate = null;
        // XXX different flow?!!
        _session = session;// null;

        _loadingURL = null;
        _orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; // UIDeviceOrientationUnknown;
        _showingKeyboard = false;

        // http://groups.google.com/group/android-developers/browse_thread/thread/a0b71c59fb33b94a/5d996451f43f507b?lnk=gst&q=ondraw#5d996451f43f507b
        setWillNotDraw(false);

        int contentPadding = kPadding + kBorderWidth;
        setPadding(contentPadding, contentPadding, contentPadding, contentPadding);

        // main content of popup window
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Color.WHITE);
        content.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        RelativeLayout title = new RelativeLayout(context);
        title.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        _titleLabel = new TextView(context);
        _titleLabel.setText(kDefaultTitle);
        _titleLabel.setBackgroundColor(kFacebookBlue);
        _titleLabel.setTextColor(Color.WHITE);
        _titleLabel.setTypeface(Typeface.DEFAULT_BOLD);
        _titleLabel.setPadding(kTitleMarginX, kTitleMarginY, kTitleMarginX, kTitleMarginY);
        _titleLabel.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // FB icon on the left side of the dialog title
        Drawable iconDrawable = CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/fbicon.png");

        // close icon on the right side of the dialog title
        Drawable closeDrawable = CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/close.png");

        // FB icon is part of TextWiev
        _titleLabel.setCompoundDrawablePadding(5); // TODO - check correct
        // padding
        _titleLabel.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
        title.addView(_titleLabel);

        // close icon is on standalone button, next to title TextView
        _closeButton = new ImageButton(context);
        _closeButton.setBackgroundColor(Color.TRANSPARENT);
        _closeButton.setImageDrawable(closeDrawable);
        _closeButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                    _titleLabel.setBackgroundColor(kBorderGray);
                    return true;
                case MotionEvent.ACTION_UP:
                    _titleLabel.setBackgroundColor(kFacebookBlue);
                    dismiss(true);
                    return true;
                }
                return false;
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        title.addView(_closeButton, lp);

        content.addView(title);

//         mProgressWrapper = new RelativeLayout(context);
//         mProgressWrapper.setLayoutParams(new
//         LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//         ViewGroup.LayoutParams.FILL_PARENT));
//         ProgressBar progressBar = new ProgressBar(context);
//         progressBar.setLayoutParams(new
//         LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//         ViewGroup.LayoutParams.WRAP_CONTENT));
//         lp = new
//         RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
//         RelativeLayout.LayoutParams.WRAP_CONTENT);
//         lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
//         lp.addRule(RelativeLayout.CENTER_VERTICAL);
//         mProgressWrapper.addView(progressBar, lp);
//         content.addView(mProgressWrapper);

        _webView = new WebView(context);        
        _webView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        _webView.setWebViewClient(new WebViewClientImpl());

        WebSettings webSettings = _webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");

        content.addView(_webView);

        addView(content);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // View

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect canvasClipBounds = new Rect(canvas.getClipBounds());

        Rect grayRect = new Rect(canvasClipBounds);
        grayRect.inset(kPadding, kPadding);
        drawRect(canvas, grayRect, kBorderGray, 10f);

        // Rect headerRect = new Rect(grayRect);
        // headerRect.inset(kBorderWidth, kBorderWidth);
        // drawRect(canvas, headerRect, kFacebookBlue, 0f);
        // strokeLines(canvas, headerRect, kBorderBlue);

        // RectF webRect = new RectF(contentOfset, contentOfset +
        // headerRect.height(), width - kBorderWidth * 2,
        // 80/*_webView.getHeight() + 1*/);
        // strokeLines(canvas, webRect, kBorderBlack);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // WebViewClient

    private final class WebViewClientImpl extends WebViewClient {
    	
    	private Dialog dialog = null;
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            URI uri;
            try {
                uri = new URI(url);

                Log.d("FBDialog", "Web view URL = " + url);
                
                // see issue #2
                if (!uri.isAbsolute()) {
                    Log.e(LOG, "Something went wrong. You probably forgot to specify API key and secret?");
                    // I don't return false from here, because I prefer nasty
                    // NPE over 'Page not available' message in browser
                }

                // fbconnect is not always the scheme but sometimes after
                // hostname
                if (uri.getScheme() != null && url.contains("fbconnect:")) {
                    if (url.contains("fbconnect://cancel") || url.contains("fbconnect:cancel")) {
                        dismissWithSuccess(false, true);
                    } else {
                        dialogDidSucceed(uri);
                    }
                    return true;
                } else if (_loadingURL.toExternalForm().equals(url)) {
                    return false;
                } else {// if (navigationType ==
                    // UIWebViewNavigationTypeLinkClicked) {
                    if (_delegate != null && !_delegate.shouldOpenURLInExternalBrowser(FBDialog.this, uri.toURL())) {
                        return true;
                    }

                    // [[UIApplication sharedApplication] openURL:request.URL];
                    // return false;
                    // } else {
                    return false;
                    // }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	super.onPageStarted(view, url, favicon);
        	
             FBProgressDialog.show(FBDialog.this.getContext(), null);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // content.removeView(mProgressWrapper);
            // content.removeView(_webView); // XXX happened on some error to me
            // that addView(webView) failed
            // content.addView(_webView);
            FBProgressDialog.hide(FBDialog.this.getContext());
        }

        // - (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError
        // *)error {
        // // 102 == WebKitErrorFrameLoadInterruptedByPolicyChange
        // if (!([error.domain isEqualToString:@"WebKitErrorDomain"] &&
        // error.code == 102)) {
        // [self dismissWithError:error animated:YES];
        // }
        // }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // // UIDeviceOrientationDidChangeNotification
    //
    // - (void)deviceOrientationDidChange:(void*)object {
    // UIDeviceOrientation orientation = [UIApplication
    // sharedApplication].statusBarOrientation;
    // if (!_showingKeyboard && [self shouldRotateToOrientation:orientation]) {
    // [self updateWebOrientation];
    //
    // CGFloat duration = [UIApplication
    // sharedApplication].statusBarOrientationAnimationDuration;
    // [UIView beginAnimations:nil context:nil];
    // [UIView setAnimationDuration:duration];
    // [self sizeToFitOrientation:YES];
    // [UIView commitAnimations];
    // }
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // // UIKeyboardNotifications
    //
    // - (void)keyboardWillShow:(NSNotification*)notification {
    // UIInterfaceOrientation orientation = [UIApplication
    // sharedApplication].statusBarOrientation;
    // if (UIInterfaceOrientationIsLandscape(orientation)) {
    // _webView.frame = CGRectInset(_webView.frame,
    // -(kPadding + kBorderWidth),
    // -(kPadding + kBorderWidth) - _titleLabel.frame.size.height);
    // }
    //
    // _showingKeyboard = YES;
    // }
    //
    // - (void)keyboardWillHide:(NSNotification*)notification {
    // UIInterfaceOrientation orientation = [UIApplication
    // sharedApplication].statusBarOrientation;
    // if (UIInterfaceOrientationIsLandscape(orientation)) {
    // _webView.frame = CGRectInset(_webView.frame,
    // kPadding + kBorderWidth,
    // kPadding + kBorderWidth + _titleLabel.frame.size.height);
    // }
    //
    // _showingKeyboard = NO;
    // }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // public

    public String getTitle() {
        return _titleLabel.getText().toString();
    }

    public void setTitle(String title) {
        _titleLabel.setText(title);
    }

    public void show() {
        // ScaleAnimation scale = new ScaleAnimation(0.001f, 1.1f, 0.001f, 1.1f,
        // ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
        // ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        // scale.setDuration((long) (kTransitionDuration / 1.5));
        //
        // // AnimationSet have bug at this time, so using listeners, as
        // // recommended on forum by Romain Guy
        // scale.setAnimationListener(new AnimationListener() {
        // public void onAnimationEnd(Animation arg0) {
        // bounce1AnimationStopped();
        // }
        //
        // public void onAnimationRepeat(Animation arg0) {
        // }
        //
        // public void onAnimationStart(Animation arg0) {
        // }
        //
        // });
        //
        // startAnimation(scale);

        load();

        // transform = CGAffineTransformScale([self transformForOrientation],
        // 0.001, 0.001);
        // [UIView beginAnimations:nil context:nil];
        // [UIView setAnimationDuration:kTransitionDuration/1.5];
        // [UIView setAnimationDelegate:self];
        // [UIView
        // setAnimationDidStopSelector:@selector(bounce1AnimationStopped)];
        // self.transform = CGAffineTransformScale([self
        // transformForOrientation], 1.1, 1.1);
        // [UIView commitAnimations];
        //    
        // [self addObservers];
    }

    protected void dismissWithSuccess(boolean success, boolean animated) {
        if (_delegate != null) {
            if (success) {
                _delegate.dialogDidSucceed(this);
            } else {
                _delegate.dialogDidCancel(this);
            }
        }
        dismiss(animated);
    }

    protected void dismissWithError(Throwable error, boolean animated) {
        _delegate.didFailWithError(this, error);
        dismiss(animated);
    }

    protected void load() {
        // Intended for subclasses to override
    }

    protected void loadURL(String url, String method, Map<String, String> getParams, Map<String, String> postParams) throws MalformedURLException {
        // by default we send the cookies
        loadURL(url, method, getParams, postParams, true);
    }

    protected void loadURL(String url, String method, Map<String, String> getParams, Map<String, String> postParams, boolean sendCookies) throws MalformedURLException {

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        _loadingURL = generateURL(url, getParams);

        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) _loadingURL.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            if (method != null) {
                conn.setRequestMethod(method);
                if ("POST".equals(method)) {
                    String contentType = "multipart/form-data; boundary=" + kStringBoundary;
                    conn.setRequestProperty("Content-Type", contentType);
                }

                // Cookies are used in FBPermissionDialog and FBFeedDialog to
                // retrieve logged user
                if (sendCookies && "POST".equals(method))
                    conn.setRequestProperty("Cookie", cookieManager.getCookie(url));

                conn.connect();

                out = conn.getOutputStream();
                if ("POST".equals(method)) {
                    String body = generatePostBody(postParams);
                    if (body != null) {
                        out.write(body.getBytes("UTF-8"));
                    }
                }
                in = conn.getInputStream();

                String response = CcUtil.getResponse(in).toString();
                if (response != "") {
                    // In case there are several Set-cookie fields in the header
                    for (int i = 0; true; i++) {
                        String hdrKey = conn.getHeaderFieldKey(i);
                        String hdrVal = conn.getHeaderField(i);
                        if (hdrKey == null) {
                            if (hdrVal == null)
                                break; // end of the header
                            continue; // in some implementations, first header
                                      // key is empty
                        }
                        Log.i(LOG, "url header: " + hdrKey + "=" + hdrVal);
                        if (hdrKey.equalsIgnoreCase("set-cookie")) {
                            cookieManager.setCookie(url, hdrVal);
                        }
                    }
                    CookieSyncManager.createInstance(mContext).sync();

                    URI uri = new URI(url);
                    // we need to load the data with base URL or else the
                    // webview doesn't know how to build relative URLs
                    _webView.loadDataWithBaseURL("http://" + uri.getHost(), response, "text/html", "UTF-8", "http://" + uri.getHost());

                    // _webView.loadData(response, "text/html", "UTF-8");
                } else if (sendCookies) {
                    // If the page has not loaded the first time we try to
                    // reload it without sending the cookies
                    // the issue is we can't delete the cookies on logout...
                    loadURL(url, method, getParams, postParams, false);
                }
            }
        } catch (URISyntaxException e) {
            Log.e(LOG, "Error on url format", e);
        } catch (IOException e) {
            Log.e(LOG, "Error while opening page", e);
            // If the page has not loaded the first time we try to reload it without sending the cookies
	    // the issue is we can't delete the cookies on logout...
	    if (sendCookies) {
	    	loadURL(url, method, getParams, postParams, false);
	    }
	} finally {
            CcUtil.close(in);
            CcUtil.close(out);
            CcUtil.disconnect(conn);
        }

    }

    protected void dialogWillAppear() {
    }

    protected void dialogWillDisappear() {
    }

    protected void dialogDidSucceed(URI uri) {
        dismissWithSuccess(true, true);
    }

    public static abstract class FBDialogDelegate {

        /**
         * Called when the dialog succeeds and is about to be dismissed.
         */
        protected void dialogDidSucceed(FBDialog dialog) {
        }

        /**
         * Called when the dialog is cancelled and is about to be dismissed.
         */
        protected void dialogDidCancel(FBDialog dialog) {
        }

        /**
         * Called when dialog failed to load due to an error.
         */
        protected void didFailWithError(FBDialog dialog, Throwable error) {
        }

        /**
         * Asks if a link touched by a user should be opened in an external
         * browser.
         * 
         * If a user touches a link, the default behavior is to open the link in
         * the Safari browser, which will cause your app to quit. You may want
         * to prevent this from happening, open the link in your own internal
         * browser, or perhaps warn the user that they are about to leave your
         * app. If so, implement this method on your delegate and return NO. If
         * you warn the user, you should hold onto the URL and once you have
         * received their acknowledgement open the URL yourself using
         * [[UIApplication sharedApplication] openURL:].
         */
        protected boolean shouldOpenURLInExternalBrowser(FBDialog dialog, URL url) {
            return false;
        }

    }

}
