/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.rest.impl;

public class TranslationInfo {

    public String cmp;
    public String key;
    public String value;

    public TranslationInfo(String cmp, String key, String value) {
        this.cmp = cmp;
        this.key = key;
        this.value = value;
    }
}
