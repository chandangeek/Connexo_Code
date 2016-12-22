package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.WebPortalAttributes;
import com.energyict.dlms.cosem.attributes.WebPortalPasswordAttributes;
import com.energyict.dlms.cosem.methods.WebPortalMethods;
import com.energyict.dlms.cosem.methods.WebPortalPasswordMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class WebPortalConfig extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.13.255");
    public static final Pattern PORT_PATTERN = Pattern.compile("[1-9][0-9]{0,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5]");
    private static final int NUMBER_MAX_LENGTH = String.valueOf(Long.MAX_VALUE).length();
    /**
     * Creates a new instance of AbstractCosemObject
     */
    public WebPortalConfig(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.WEB_PORTAL_CONFIGURATION.getClassId();
    }

    public void changeUser1Password(String newPassword) throws IOException {
        methodInvoke(WebPortalPasswordMethods.CHANGE_USER_1_PASSWORD, OctetString.fromString(newPassword));
    }

    public void changeUser2Password(String newPassword) throws IOException {
        methodInvoke(WebPortalPasswordMethods.CHANGE_USER_2_PASSWORD, OctetString.fromString(newPassword));
    }

    public void changeUserPassword(String userName, String newPassword) throws IOException {
        byte[] loginData = getUserLoginData(userName, newPassword);

        methodInvoke(WebPortalMethods.CHANGE_USER_PASSWORD, OctetString.fromByteArray(loginData, loginData.length));
    }

    private byte[] getUserLoginData(String user, String newPassword) {
        byte[] password = newPassword.getBytes();
        byte[] userName = user.getBytes();
        byte[] loginData = new byte[userName.length + password.length];

        System.arraycopy(userName, 0, loginData, 0, userName.length);
        System.arraycopy(password, 0, loginData, userName.length, password.length);
        return loginData;
    }

    public void setHttpPort(String httpPort) throws IOException {
        Matcher m = PORT_PATTERN.matcher( httpPort );
        if (m.find()) {
            write(WebPortalAttributes.HTTP_PORT, new Unsigned16(Integer.parseInt(httpPort)));
        }
    }

    public void setHttpsPort(String httpsPort) throws IOException {
        Matcher m = PORT_PATTERN.matcher(httpsPort);
        if (m.find()) {
            write(WebPortalAttributes.HTTPS_PORT, new Unsigned16(Integer.parseInt(httpsPort)));
        } else {
            throw new IllegalArgumentException("HTTP Port value is invalid");
        }
    }

    public void setMaxLoginAttempts(String loginAttempts) throws IOException {
        if (isPositiveNumber(loginAttempts)) {
            Structure structure = new Structure();
            structure.addDataType(new Unsigned32(Long.parseLong(loginAttempts)));
            write(WebPortalAttributes.MAX_LOGIN_ATTEMPTS, structure.getBEREncodedByteArray());
        } else {
            throw new IllegalArgumentException("Login Attempts value is invalid");
        }
    }

    public void setLockoutDuration(String duration) throws IOException {
        if (isPositiveNumber(duration)) {
            Structure structure = new Structure();
            structure.addDataType(new Unsigned32(Long.parseLong(duration)));
            write(WebPortalAttributes.LOCKOUT_DURATION, structure.getBEREncodedByteArray());
        } else {
            throw new IllegalArgumentException("Lockout Duration value is invalid");
        }
    }

    public void enableGzipCompression(Boolean enableGzip) throws IOException {
        methodInvoke(WebPortalMethods.ENABLE_GZIP, new BooleanObject(enableGzip).getBEREncodedByteArray());
    }

    public void enableSSL(Boolean enableSSL) throws IOException {
        methodInvoke(WebPortalMethods.ENABLE_SSL, new BooleanObject(enableSSL).getBEREncodedByteArray());
    }

    public void setAuthenticationMechanism(int auth) throws IOException {
        Structure structure = new Structure();
        structure.addDataType(new Unsigned32(auth));
        write(WebPortalAttributes.AUTHENTICATION_MECHANISM, structure.getBEREncodedByteArray());
    }

    public static boolean isPositiveNumber(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        if (string.length() >= NUMBER_MAX_LENGTH) {
            try {
                Long.parseLong(string);
            } catch (Exception e) {
                return false;
            }
        } else {
            for (int i = 0; i < string.length(); i++) {
                if (!Character.isDigit(string.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public OctetString readLoginUser1() throws IOException {
        return readDataType(WebPortalPasswordAttributes.LOGIN_USER_1, OctetString.class);
    }

    public OctetString readLoginUser2() throws IOException {
        return readDataType(WebPortalPasswordAttributes.LOGIN_USER_2, OctetString.class);
    }

    public OctetString readLoginUser() throws IOException {
        return readDataType(WebPortalAttributes.USER_NAME, OctetString.class);
    }

    public void enableInterfaces(Array interfacesArray) throws IOException {
        write(WebPortalAttributes.ENABLED_INTERFACES, interfacesArray.getBEREncodedByteArray());
    }
}