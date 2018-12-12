/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.UserPreference;

public class UserPreferenceInfo {
    
    public String key;
    public String value;

    public UserPreferenceInfo() {
    }

    public UserPreferenceInfo(UserPreference preference) {
        this.key = preference.getType().getTranslationKey();
        this.value = preference.getDisplayFormat();
    }
}