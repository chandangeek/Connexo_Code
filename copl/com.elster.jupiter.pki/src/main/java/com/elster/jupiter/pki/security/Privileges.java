/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum Privileges implements TranslationKey {
    // Resources
    RESOURCE_CERTIFICATE("pki.certificateMgmt", "Certificate management"),
    RESOURCE_CERTIFICATES_DESCRIPTION("pki.certificates.description", "Manage trust stores and certificates"),

    RESOURCE_SECURITY_ACCESSOR_MANAGEMENT("pki.securityAccessorManagement", "Security accessors management"),
    RESOURCE_SECURITY_ACCESSOR_MANAGEMENT_DESCRIPTION("pki.securityAccessorManagement.description", "Manage security accessors"),

    RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES("pki.securityAccessorAttributes", "Security accessor attributes"),
    RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES_DESCRIPTION("pki.securityAccessorAttributes.description", "Manage attributes of security accessors"),

    // Certificate privileges
    VIEW_CERTIFICATES(Constants.VIEW_CERTIFICATES, "View"),
    ADMINISTRATE_CERTIFICATES(Constants.ADMINISTRATE_CERTIFICATES, "Administrate certificates"),
    ADMINISTRATE_TRUST_STORE(Constants.ADMINISTRATE_TRUST_STORES, "Administrate trust stores"),

    // Security accessor privileges
    VIEW_SECURITY_ACCESSORS(Constants.VIEW_SECURITY_ACCESSORS, "View"),
    EDIT_SECURITY_ACCESSORS(Constants.EDIT_SECURITY_ACCESSORS, "Administer"),

    // Privileges for security accessor attributes
    VIEW_SECURITY_PROPERTIES_1(Constants.VIEW_SECURITY_PROPERTIES_1, "View level 1"),
    VIEW_SECURITY_PROPERTIES_2(Constants.VIEW_SECURITY_PROPERTIES_2, "View level 2"),
    VIEW_SECURITY_PROPERTIES_3(Constants.VIEW_SECURITY_PROPERTIES_3, "View level 3"),
    VIEW_SECURITY_PROPERTIES_4(Constants.VIEW_SECURITY_PROPERTIES_4, "View level 4"),
    EDIT_SECURITY_PROPERTIES_1(Constants.EDIT_SECURITY_PROPERTIES_1, "Edit level 1"),
    EDIT_SECURITY_PROPERTIES_2(Constants.EDIT_SECURITY_PROPERTIES_2, "Edit level 2"),
    EDIT_SECURITY_PROPERTIES_3(Constants.EDIT_SECURITY_PROPERTIES_3, "Edit level 3"),
    EDIT_SECURITY_PROPERTIES_4(Constants.EDIT_SECURITY_PROPERTIES_4, "Edit level 4");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .toArray(String[]::new);
    }

    public interface Constants {
        String VIEW_CERTIFICATES = "privilege.view.certificates";
        String ADMINISTRATE_CERTIFICATES = "privilege.administrate.certificates";
        String ADMINISTRATE_TRUST_STORES = "privilege.administrate.trust.store";

        String VIEW_SECURITY_ACCESSORS = "view.security.accessors";
        String EDIT_SECURITY_ACCESSORS = "edit.security.accessors";

        String VIEW_SECURITY_PROPERTIES_1 = "view.security.properties.level1";
        String VIEW_SECURITY_PROPERTIES_2 = "view.security.properties.level2";
        String VIEW_SECURITY_PROPERTIES_3 = "view.security.properties.level3";
        String VIEW_SECURITY_PROPERTIES_4 = "view.security.properties.level4";

        String EDIT_SECURITY_PROPERTIES_1 = "edit.security.properties.level1";
        String EDIT_SECURITY_PROPERTIES_2 = "edit.security.properties.level2";
        String EDIT_SECURITY_PROPERTIES_3 = "edit.security.properties.level3";
        String EDIT_SECURITY_PROPERTIES_4 = "edit.security.properties.level4";
    }
}
