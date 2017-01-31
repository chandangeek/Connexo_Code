/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elster.jupiter.users.UserPreference;

public class UserPreferenceInfos {
    
    public List<UserPreferenceInfo> preferences;

    public UserPreferenceInfos() {
    }

    public UserPreferenceInfos(List<UserPreference> preferences) {
        this.preferences = preferences.stream().map(UserPreferenceInfo::new).collect(Collectors.toList());
    }
}