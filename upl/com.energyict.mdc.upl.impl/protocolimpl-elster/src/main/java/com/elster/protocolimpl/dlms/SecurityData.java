package com.elster.protocolimpl.dlms;

/**
 * Class holding security information
 * <p/>
 * Date: 01.08.11
 * Time: 13:39
 */
@SuppressWarnings({"unused"})
public class SecurityData {

    public String securityLevel;

    public String password;
    public String authKey;
    public String encKey;

    public SecurityData(String dlmsSecurityLevel) {
        securityLevel = dlmsSecurityLevel;
    }

    public int getAuthenticationLevel() {
        String[] data = securityLevel.split(":");
        return Integer.parseInt(data[0]);
    }

    public int getEncryptionLevel() {
        String[] data = securityLevel.split(":");
        return Integer.parseInt(data[1]);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthenticationKey() {
        return authKey;
    }

    public void setAuthenticationKey(String authKey) {
        this.authKey = authKey;
    }

    public String getEncryptionKey() {
        return encKey;
    }

    public void setEncryptionKey(String encKey) {
        this.encKey = encKey;
    }

    public String checkSecurityData() {
        int al = getAuthenticationLevel();
        int el = getEncryptionLevel();

        if (((al == 0) || (al == 1)) && (el == 0)) {
            return "";
        }

        if (((al == 5) && (el == 1)) ||
                ((al == 5) && (el == 3))) {
            String msg;
            msg = checkKey(authKey, "authentication key");
            if (msg.length() != 0) {
                return msg;
            }
            if (el == 1) {
                return "";
            }

            msg = checkKey(encKey, "encryption key");
            if (msg.length() != 0) {
                return msg;
            }
            return "";
        }
        return "Invalid DLMS security level definition";
    }

    public static String checkKey(String keyValue, String keyName) {

        if ((keyValue == null) || (keyValue.length() == 0)) {
            return keyName + " is null or empty";
        }

        if (keyValue.length() != 32) {
            return "Wrong length for " + keyName;
        }

        try {
            for (int i = 0; i < keyValue.length(); i += 2) {
                String b = keyValue.substring(i, i + 2);
                Integer.parseInt(b, 16);
            }
        } catch (NumberFormatException nfe) {
            return "Wrong character in " + keyName;
        }
        return "";
    }
}
