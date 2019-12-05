/*
 * ThirdPartyInfo.java
 *
 * Created on 18 oktober 2004, 13:27
 */

package com.energyict.mdc.engine.offline.gui.util;

/**
 * @author Geert
 */
public class ThirdPartyInfo {

    private String name;
    private String url;
    private String licenseText;

    /**
     * Creates a new instance of ThirdPartyInfo
     */
    public ThirdPartyInfo(String theName, String theUrl, String theLicenseText) {
        name = theName;
        url = theUrl;
        licenseText = theLicenseText;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getLicenseText() {
        return licenseText;
    }
}
