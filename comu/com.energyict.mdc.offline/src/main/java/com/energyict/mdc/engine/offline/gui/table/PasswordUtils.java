package com.energyict.mdc.engine.offline.gui.table;

import com.energyict.mdc.engine.offline.gui.core.PatchedJPasswordField;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A number of static functions to convert Strings, especially URI containing passwords.
 * Password should not be displayed but 'hidden beyond echo characters'. Because we make use
 * of a PatchedJPasswordField (a UI Component) to determine which echo character to use, and because
 * this class will be used by renderers and textcomponents, this utility class is put in the cso package.
 */
public class PasswordUtils {

    /**
     * Replaces the password in the userinfo of an <Code>URI</Code> by the echocharacter of a JPasswordField
     *
     * @param uri    :the <Code>URI</Code> to hide the password from
     * @param length : the number of characters to replace the password with
     * @return a String where the password is replaced by a number of echo characters specified by length
     * @throws URISyntaxException when a invalid uri was passed
     */
    public static String hidePassword(String uri, int length) throws URISyntaxException {
        String userInfo;
        String uriCopy = uri;

        if (uri.substring(0,4).equalsIgnoreCase("sftp")) {
            uriCopy = uri.substring(1);  //change sftp into ftp, so it can be handled by URL()
        }

        try {
            URL actionUrl = new URL(uriCopy);
            userInfo = actionUrl.getUserInfo();
        } catch (MalformedURLException exc) {
            URI actionUri = new URI(uriCopy);       // URL does not have support for sftp/ftps protocols
            userInfo = actionUri.getUserInfo();
        }

        if (userInfo == null) {
            return uri;
        }
        return uri.replaceFirst(userInfo, hidePasswordInUserInfo(userInfo));

    }

    /**
     * Replaces the password in the userinfo of an <Code>URI</Code> by the echocharacter of a JPasswordField
     *
     * @param uri :the <Code>URI</Code> to hide the password from
     * @return a String where the password is replaced by a 5 echo characters
     * @throws URISyntaxException when a invalid uri was passed
     */
    public static String hidePassword(String uri) throws URISyntaxException {
        return hidePassword(uri, 5);
    }

    /**
     * Replaces the userinfo of an <Code>URI</Code> by the echocharacter of a JPasswordField
     *
     * @param uri    :the <Code>URI</Code> to hide the password from
     * @param length : the number of characters to replace the userinfo with
     * @return a String where the userinfo is replaced by a number of echo characters specified by length
     * @throws URISyntaxException when a invalid uri was passed
     */
    public static String hideUserInfo(String uri, int length) throws URISyntaxException {
        char passwordChar = getDefaultPasswordChar();
        String userInfo;

        try {
            URL actionUrl = new URL(uri);
            userInfo = actionUrl.getUserInfo();
        } catch (MalformedURLException exc) {
            URI actionUri = new URI(uri);       // URL does not have support for sftp/ftps protocols
            userInfo = actionUri.getUserInfo();
        }

        if (userInfo == null) {
            return uri;
        }

        return uri.replaceAll(userInfo, repeat(passwordChar, length));
    }

    /**
     * Replaces the userinfo of an <Code>URI</Code> by the echocharacter of a JPasswordField
     *
     * @param uri :the <Code>URI</Code> to hide the password from
     * @return a String where the userinfo is replaced by a 5 echo characters
     * @throws URISyntaxException when a invalid uri was passed
     */
    public static String hideUserInfo(String uri) throws URISyntaxException {
        return hideUserInfo(uri, 5);
    }

    private static String hidePasswordInUserInfo(String userInfo) {
        char passwordChar = getDefaultPasswordChar();
        String[] array = userInfo.split(":");
        if (array.length <= 1) {
            return userInfo;
        }
        return array[0] + ":" + repeat(passwordChar, 5);
    }

    public static char getDefaultPasswordChar() {
        PatchedJPasswordField passwordField = new PatchedJPasswordField();
        return passwordField.getEchoChar();
    }

    public static String repeat(char c, int i) {
        String result = "";
        for (int j = 0; j < i; j++)
	        {
	    	result = result+c;
	    }
	    return result;
    }
	
	

}
