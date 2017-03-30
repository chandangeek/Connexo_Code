/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_CERTIFICATE("pki.certificateMgmt", "Certificate management"),
    RESOURCE_CERTIFICATES_DESCRIPTION("pki.certificates.description", "Manage trust stores and certificates"),

    //Privileges
    VIEW_CERTIFICATES(Constants.VIEW_CERTIFICATES, "View"),
    ADMINISTRATE_CERTIFICATES(Constants.ADMINISTRATE_CERTIFICATES, "Administrate certificates"),
    ADMINISTRATE_TRUST_STORE(Constants.ADMINISTRATE_TRUST_STORES, "Administrate trust stores"),
    ;

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

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
    }
}
