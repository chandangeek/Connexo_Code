/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow.identity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Created by dragos on 11/17/2015.
 */
public class ConnexoFlowRestProxyManager {

    private static final String CONNEXO_CONFIG = System.getProperty("connexo.configuration");

    private final String url;
    private final String user;
    private final String password;

    private static ConnexoFlowRestProxyManager instance = null;

    public static synchronized ConnexoFlowRestProxyManager getInstance() {
        if (instance == null) {
            Properties properties = new Properties();
            if (CONNEXO_CONFIG != null) {
                try {
                    FileInputStream inputStream = new FileInputStream(CONNEXO_CONFIG);
                    properties.load(inputStream);
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String property = properties.getProperty("com.elster.jupiter.url");
            String url_param = (property != null) ? property : "http://localhost:8080";

            property = properties.getProperty("com.elster.jupiter.user");
            String user_param = (property != null) ? property : "admin";

            property = properties.getProperty("com.elster.jupiter.password");
            String password_param = (property != null) ? property : "admin";

            instance = new ConnexoFlowRestProxyManager(url_param, user_param, password_param);
        }

        return instance;
    }

    private ConnexoFlowRestProxyManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public boolean existsUser(String userId) {
        if (userId.equals("Administrator")) {
            return true;
        }

        try {
            return (getEntity("/api/usr/findusers/" + URLEncoder.encode(userId, "UTF-8")) != null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsGroup(String groupId) {
        if (groupId.equals("Administrators")) {
            return true;
        }

        try {
            return (getEntity("/api/usr/findworkgroups/" + URLEncoder.encode(groupId, "UTF-8")) != null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getMembersOf(String groupId) {
        if (groupId.equals("Administrators")) {
            return new ArrayList<String>() {{
                add("Administrator");
            }};
        }

        List<String> members = new ArrayList<>();
        JSONArray array = null;
        try {
            array = (JSONArray) getEntities("/api/usr/findworkgroups/" + URLEncoder.encode(groupId, "UTF-8") + "/users", "users");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject user = (JSONObject) array.get(i);
                    if (user != null) {
                        members.add(user.getString("authenticationName"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return members;
    }

    public List<String> getGroupsOf(String userId) {
        if (userId.equals("Administrator")) {
            return new ArrayList<String>() {{
                add("Administrators");
            }};
        }
        List<String> members = new ArrayList<>();
        JSONArray array = null;
        try {
            array = (JSONArray) getEntities("/api/usr/findusers/" + URLEncoder.encode(userId, "UTF-8") + "/workgroups", "workGroups");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject group = (JSONObject) array.get(i);
                    if (group != null) {
                        members.add(group.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return members;
    }

    public String getLanguageOf(String userId) {
        if (userId.equals("Administrator")) {
            return "en_US";
        }
        JSONObject object = null;
        try {
            object = (JSONObject) getEntity("/api/usr/findusers/" + URLEncoder.encode(userId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (object != null) {
            try {
                JSONObject language = (JSONObject) object.get("language");
                if (language != null) {
                    return language.getString("languageTag");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "en_US";
    }

    private Object getEntity(String targetUrl) {
        String jsonContent = null;
        JSONObject object = null;

        try {
            jsonContent = doGet(targetUrl);
            if (!"".equals(jsonContent)) {
                object = new JSONObject(jsonContent);
                if (object != null) {
                    return object;
                } else {
                    throw new RuntimeException("No entity found at " + targetUrl);
                }
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private Object getEntities(String targetUrl, String element) {
        String jsonContent = null;
        JSONArray array = null;

        try {
            jsonContent = doGet(targetUrl);
            if (!"".equals(jsonContent)) {
                JSONObject object = new JSONObject(jsonContent);
                if (object != null) {
                    array = object.getJSONArray(element);
                    if (array != null && array.length() > 0) {
                        return array;
                    }
                } else {
                    throw new RuntimeException("No entity found at " + targetUrl);
                }
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace().toString());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace().toString());
        }

        return null;
    }

    private String doGet(String targetURL) {
        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.user + ":" + this.password).getBytes()));
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() < 200 || httpConnection.getResponseCode() >= 300) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConnection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
            StringBuilder jsonContent = new StringBuilder();
            while ((output = br.readLine()) != null) {
                jsonContent.append(output);
            }
            return jsonContent.toString();

        } catch (IOException e) {
            throw new RuntimeException(e.getStackTrace().toString());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }


}
