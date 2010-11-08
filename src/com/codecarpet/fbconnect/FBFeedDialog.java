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
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

public class FBFeedDialog extends FBDialog {

    private static final String kPublishURL = "http://www.facebook.com/connect/prompt_feed.php";

    private String[] _permissions;
    private String _messagePrompt;
    private String _attachment;
    private String _actionLinks;
    private String _targetId;

    public FBFeedDialog(Activity context, FBSession session, String messagePrompt, String attachment, String actionLinks, String targetId) {
        super(context, session);
        _messagePrompt = messagePrompt;
        _attachment = attachment;
        _actionLinks = actionLinks;
        _targetId = targetId;
    }

    private void loadExtendedPermissionPage() {
        Map<String, String> getParams = new HashMap<String, String>();
        getParams.put("display", "touch");
        getParams.put("callback", "fbconnect://success");
        getParams.put("cancel", "fbconnect://cancel");
        
        Map<String, String> postParams = new HashMap<String, String>();

        postParams.put("api_key", _session.getApiKey());
        postParams.put("session_key", _session.getSessionKey());
        postParams.put("preview", "1");
        postParams.put("attachment", _attachment);
        postParams.put("action_links", _actionLinks);
        postParams.put("target_id", _targetId);
        postParams.put("user_message_prompt", _messagePrompt);

        try {
            loadURL(kPublishURL, "POST", getParams, postParams);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void load() {
        loadExtendedPermissionPage();
    }
}
