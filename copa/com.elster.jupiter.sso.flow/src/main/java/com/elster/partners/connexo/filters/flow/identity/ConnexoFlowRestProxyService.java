package com.elster.partners.connexo.filters.flow.identity;

import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ConnexoFlowRestProxyService {

    private String url;
    private String user;
    private String password;
    private String token;

    public ConnexoFlowRestProxyService() {
        this.url = System.getProperty("com.elster.jupiter.url");
        this.user = System.getProperty("com.elster.jupiter.user");
        this.password = System.getProperty("com.elster.jupiter.password");
        this.token = System.getProperty("com.elster.jupiter.token");
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
                return object;
            }
        } catch (JSONException | RuntimeException e) {
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
                array = object.getJSONArray(element);
                if (array != null && array.length() > 0) {
                    return array;
                }
            }
        } catch (JSONException | RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
            if (this.token != null && !this.token.isEmpty()) {
                httpConnection.setRequestProperty("Authorization", "Bearer " + this.token);
            } else {
                httpConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((this.user + ":" + this.password).getBytes()));
            }
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
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private String doPost(String targetURL, String username, String password) {
        HttpURLConnection httpConnection = null;
        try {
            URL connexoUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) connexoUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes()));
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 204) {
                return null;
            }

            return httpConnection.getHeaderField("X-AUTH-TOKEN");

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public User authenticate(String username, String password) {

        String authorization = doPost("/api/apps/apps/login", username, password);
        if (authorization != null) {
            Set<Role> roles = new HashSet<Role>();
            RoleImpl defaultAdmin = new RoleImpl("admin");
            if (!roles.contains(defaultAdmin)) {
                roles.add(defaultAdmin);
            }

            return new UserImpl(username, roles, new HashSet<Group>());
        }

        return null;
    }
}
