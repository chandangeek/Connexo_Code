package com.elster.partners.connexo.filters.flow.identity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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

    boolean findUser(String userId) {
        // TODO: implement this URL
        return (getEntities("api/usr/finduser/" + userId, false) != null);
    }

    boolean findGroup(String groupId) {
        // TODO: implement this URL
        return (getEntities("api/usr/findgroup/" + groupId, false) != null);
    }

    List<String> getMembersOf(String groupId) {
        // TODO: implement this URL
        List<String> members = new ArrayList<>();
        JSONArray array = (JSONArray) getEntities("api/usr/findgroup/" + groupId, true);
        for(int i=0; i<array.length(); i++) {
            try {
                members.add(array.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return members;
    }

    public List<String> getGroupsOf(String userId) {
        // TODO: implement this URL
        List<String> members = new ArrayList<>();
        JSONArray array = (JSONArray) getEntities("api/usr/finduser/" + userId + "/groups", true);
        for(int i=0; i<array.length(); i++) {
            try {
                members.add(array.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return members;
    }

    String getLanguageOf(String userId) {
        // TODO: implement this URL
        JSONObject object = (JSONObject) getEntity("api/usr/finduser/" + userId + "/language");
        return (object != null) ? object.toString() : "en_US";
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

    private Object getEntities(String targetUrl, boolean all) {
        String jsonContent = null;
        JSONArray array = null;

        try {
            jsonContent = doGet(targetUrl);
            if (!"".equals(jsonContent)) {
                array = new JSONObject(jsonContent).getJSONArray("result");
                if(array != null && array.length() > 0) {
                    return all ? array : array.get(0);
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
