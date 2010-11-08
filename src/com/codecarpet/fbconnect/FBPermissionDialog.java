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

public class FBPermissionDialog extends FBDialog {

    private static final String kPermissionURL = "http://www.facebook.com/connect/prompt_permissions.php";

    private String[] _permissions;

    public FBPermissionDialog(Activity context, FBSession session, String[] permissions) {
        super(context, session);
        _permissions = permissions;
    }

    private void loadExtendedPermissionPage() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("fbconnect", "1");
        params.put("connect_display", "touch");
        params.put("api_key", _session.getApiKey());
        params.put("next", "fbconnect://success");
        params.put("cancel", "fbconnect://cancel");

        // Building the comma separated list of permissions
        String permissionList = "";
        int permissionLength = _permissions.length;
        for (int i = 0; i < permissionLength; i++) {
            permissionList += _permissions[i] + ((i == (permissionLength - 1)) ? "" : ",");
        }

        params.put("ext_perm", permissionList);

        try {
            loadURL(kPermissionURL, "GET", params, null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void load() {
        loadExtendedPermissionPage();
    }
}
