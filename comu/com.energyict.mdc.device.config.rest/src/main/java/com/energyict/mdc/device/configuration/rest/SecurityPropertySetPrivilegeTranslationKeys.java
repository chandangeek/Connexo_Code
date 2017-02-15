/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (15:11)
 */
public enum SecurityPropertySetPrivilegeTranslationKeys implements TranslationKey {

    EDIT_1(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, "Edit level 1"),
    EDIT_2(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2, "Edit level 2"),
    EDIT_3(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3, "Edit level 3"),
    EDIT_4(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4, "Edit level 4"),
    VIEW_1(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, "View level 1"),
    VIEW_2(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, "View level 2"),
    VIEW_3(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, "View level 3"),
    VIEW_4(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4, "View level 4");

    private final DeviceSecurityUserAction action;
    private final String defaultFormat;

    SecurityPropertySetPrivilegeTranslationKeys(DeviceSecurityUserAction action, String defaultFormat) {
        this.action = action;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.action.getPrivilege();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static SecurityPropertySetPrivilegeTranslationKeys from(String privilege) {
        return Stream
                .of(values())
                .filter(each -> each.action.getPrivilege().equals(privilege))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown or unsupported security property privilege: " + privilege));
    }

    public static String translationFor(String privilege, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(privilege)).format();
    }

}