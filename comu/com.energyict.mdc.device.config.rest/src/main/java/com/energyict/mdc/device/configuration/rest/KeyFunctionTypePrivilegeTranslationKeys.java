/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.SecurityAccessorUserAction;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (15:11)
 */
public enum KeyFunctionTypePrivilegeTranslationKeys implements TranslationKey {
    // Remark: the translations SHOULD contain the "Edit" or "View" part. If not you can't distinguish them in Connexo Admin > Edit role page
    EDIT_1(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1, "Edit level 1"),
    EDIT_2(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_2, "Edit level 2"),
    EDIT_3(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_3, "Edit level 3"),
    EDIT_4(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_4, "Edit level 4"),
    VIEW_1(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_1, "View level 1"),
    VIEW_2(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_2, "View level 2"),
    VIEW_3(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_3, "View level 3"),
    VIEW_4(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_4, "View level 4");

    private final SecurityAccessorUserAction action;
    private final String defaultFormat;

    KeyFunctionTypePrivilegeTranslationKeys(SecurityAccessorUserAction action, String defaultFormat) {
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

    public static KeyFunctionTypePrivilegeTranslationKeys from(String privilege) {
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
