package com.elster.partners.connexo.filters.flow.identity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by dragos on 11/17/2015.
 */
public class ConnexoRestProxyManager {

    private final String url;
    private final String token;

    private static ConnexoRestProxyManager instance = null;

    public static synchronized ConnexoRestProxyManager getInstance(String url, String token) {
        if(instance == null) {
            instance = new ConnexoRestProxyManager(url, token);
        }

        return instance;
    }

    static ConnexoRestProxyManager getInstance() {
        return instance;
    }

    private ConnexoRestProxyManager(String url, String token) {
        this.url = url;
        this.token = token;
    }

    public boolean existsUser(String userId) {
        try {
            return (getEntity("/api/usr/findusers/" + URLEncoder.encode(userId, "UTF-8")) != null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsGroup(String groupId) {
        try {
            return (getEntity("/api/usr/findgroups/" + URLEncoder.encode(groupId, "UTF-8")) != null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getMembersOf(String groupId) {
        List<String> members = new ArrayList<>();
        JSONArray array = null;
        try {
            array = (JSONArray) getEntities("/api/usr/findgroups/" + URLEncoder.encode(groupId, "UTF-8") + "/users", "users");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(array != null) {
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
        List<String> members = new ArrayList<>();
        JSONArray array = null;
        try {
            array = (JSONArray) getEntities("/api/usr/findusers/" +  URLEncoder.encode(userId, "UTF-8") + "/groups", "groups");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(array != null) {
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
        JSONObject object = null;
        try {
            object = (JSONObject) getEntity("/api/usr/findusers/" +  URLEncoder.encode(userId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(object != null) {
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
                if(object != null) {
                    return object;
                }
                else {
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

    private Object getEntities(String targetUrl, String element) {
        String jsonContent = null;
        JSONArray array = null;

        try {
            jsonContent = doGet(targetUrl);
            if (!"".equals(jsonContent)) {
                JSONObject object = new JSONObject(jsonContent);
                if(object != null) {
                    array = object.getJSONArray(element);
                    if(array != null && array.length() > 0){
                        return array;
                    }
                }
                else {
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
            // TODO: use the proper header name here
            httpConnection.setRequestProperty("X-CONNEXO-TOKEN", this.token);
            httpConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 200) {
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
