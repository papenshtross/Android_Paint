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

import temporary.CcUtil;
import android.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.widget.ImageButton;

import com.codecarpet.fbconnect.FBSession.FBSessionDelegate;

public class FBLoginButton extends ImageButton {

    public static enum FBLoginButtonStyle {
        FBLoginButtonStyleNormal, FBLoginButtonStyleWide
    }

    private FBLoginButtonStyle _style;
    private FBSession _session;
    private FBSessionDelegate _sessionDelegate;
    // I am not sure it is a good idea to hold context here
    private Context mContext;

    public FBLoginButton(Context context) {
        super(context);
        initButton(context);
    }

    public FBLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initButton(context);
    }

    public FBLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initButton(context);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        Drawable drawable;
        int[] states = getDrawableState();

        if (StateSet.stateSetMatches(new int[] { R.attr.state_pressed }, states) || StateSet.stateSetMatches(new int[] { R.attr.state_focused }, states)) {
            drawable = buttonHighlightedImage();
        } else {
            drawable = buttonImage();
        }

        setImageDrawable(drawable);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // private

    private Drawable buttonImage() {
        if (_session.isConnected()) {
            return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/logout.png");
        } else {
            if (_style == FBLoginButtonStyle.FBLoginButtonStyleNormal) {
                return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/login.png");
            } else if (_style == FBLoginButtonStyle.FBLoginButtonStyleWide) {
                return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/login2.png");
            } else {
                return null;
            }
        }
    }

    private Drawable buttonHighlightedImage() {
        if (_session.isConnected()) {
            return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/logout_down.png");
        } else {
            if (_style == FBLoginButtonStyle.FBLoginButtonStyleNormal) {
                return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/login_down.png");
            } else if (_style == FBLoginButtonStyle.FBLoginButtonStyleWide) {
                return CcUtil.getDrawable(getClass(), "com/codecarpet/fbconnect/resources/login2_down.png");
            } else {
                return null;
            }
        }
    }

    private void updateImage() {
        invalidate();
    }

    private void touchUpInside() {
        if (_session.isConnected()) {
            _session.logout(mContext);
        } else {
            Intent intent = new Intent(mContext, FBLoginActivity.class);
            mContext.startActivity(intent);
        }
    }

    private void initButton(Context context) {
        setBackgroundColor(Color.TRANSPARENT);
        setAdjustViewBounds(true);
        _style = FBLoginButtonStyle.FBLoginButtonStyleNormal;

        setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                new Thread() {
                    public void run() {
                        touchUpInside();
                    }
                }.start();
            }
        });
        
        _session = FBSession.getSession();
        _sessionDelegate = new FBSessionDelegateImpl();
        mContext = context;
        updateImage();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////
    // public

    public void setSession(FBSession session) {
        if (session != _session) {
            _session.getDelegates().remove(_sessionDelegate);
            _session = session;
            _session.getDelegates().add(_sessionDelegate);

            updateImage();
        }
    }

    public void setStyle(FBLoginButtonStyle style) {
        _style = style;
        updateImage();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    private class FBSessionDelegateImpl extends FBSessionDelegate {

        public void session_didLogin(FBSession session, Long uid) {
            updateImage();
        }

        public void sessionDidLogout(FBSession session) {
            updateImage();
        }

    }

}
