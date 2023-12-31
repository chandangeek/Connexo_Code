/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@XmlRootElement
public class UserInfo {

    public long id;
    public String authenticationName;
    public String description;
    public Boolean active;
    public long version;
    public String domain;
    public LocaleInfo language;
    public String createdOn;
    public String modifiedOn;
    public String lastSuccessfulLogin;
    public String lastUnSuccessfulLogin;
    public int unSuccessfulLoginCount;
    public boolean isUserLocked;
    public boolean isRoleModified;
    public String email;
    public List<GroupInfo> groups = new ArrayList<>();

    public UserInfo() {
    }

    public boolean update(User user) {
        return updateEmail(user) | updateDescription(user) | updateLocale(user) | updateStatus(user) | updateRoleModified(user);
    }

    private boolean updateLocale(User user) {
        if (language == null || language.languageTag == null) {
            user.setLocale(null);
            return true;
        }
        if (!language.languageTag.equals(user.getLanguage())) {
            user.setLocale(Locale.forLanguageTag(language.languageTag));
            return true;
        }
        return false;
    }

    private boolean updateEmail(User user) {
        if(email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            return true;
        }
        return false;
    }

    private boolean updateDescription(User user) {
        if (description != null && !description.equals(user.getDescription())) {
            user.setDescription(description);
            return true;
        }
        return false;
    }

    private boolean updateStatus(User user) {
        if (active != null && !active.equals(user.getStatus())) {
            user.setStatus(active);
            return true;
        }
        return false;
    }

    public boolean updateUnsuccessfullLoginCount(User user) {
        if (unSuccessfulLoginCount != user.getUnSuccessfulLoginCount()) {
            user.setUnSuccessfulLoginCount(unSuccessfulLoginCount);
            return true;
        }
        return false;
    }

    private boolean updateRoleModified(User user) {
        if (isRoleModified != user.isRoleModified()) {
            user.setRoleModified(isRoleModified);
            return true;
        }
        return false;
    }
}
