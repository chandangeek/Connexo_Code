/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import java.util.Locale;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.MessageSeeds.Keys;
import com.elster.jupiter.users.UserPreference;

@UniqueDefaultKeyPerLocale(groups = {Save.Create.class}, message = "{" + Keys.ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED + "}")
public class UserPreferenceImpl implements UserPreference {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String locale;
    
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private PreferenceType key;
    
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String formatBE;
    
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String formatFE;
    
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private boolean isDefault;
    
    public String getLanguageTag() {
        return locale;
    }
    
    public void setLanguageTag(String languageTag) {
        this.locale = languageTag;
    }
    
    @Override
    public Locale getLocale() {
        return locale != null ? Locale.forLanguageTag(locale) : null;
    }

    @Override
    public PreferenceType getType() {
        return key;
    }
    
    public void setKey(PreferenceType key) {
        this.key = key;
    }

    @Override
    public String getFormat() {
        return formatBE;
    }
    
    public void setFormatBE(String formatBE) {
        this.formatBE = formatBE;
    }

    @Override
    public String getDisplayFormat() {
        return formatFE;
    }
    
    public void setFormatFE(String formatFE) {
        this.formatFE = formatFE;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
