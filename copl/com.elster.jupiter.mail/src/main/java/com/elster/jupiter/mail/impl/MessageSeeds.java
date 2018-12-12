/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    INCOMPLETE_MAIL_CONFIG(1, "mail.incomplete.config", "Mail configuration is incomplete, these properties are missing: {0}"),
    INVALID_ADDRESS(2, "mail.invalid.address", "Not a valid email address"),
    ;

    private  int errorCode;
    private String translationKey;
    private String defaultTranslation;

    MessageSeeds(int errorCode, String translationKey, String defaultTranslation) {
        this.errorCode = errorCode;
        this.translationKey = translationKey;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getModule() {
        return MailServiceImpl.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.errorCode;
    }

    @Override
    public String getKey() {
        return this.translationKey;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}