package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import java.text.MessageFormat;
import java.util.Objects;

public enum DefaultTranslationKey implements TranslationKey {
    PRE_TRANSITION_CHECKS_FAILED("PreTransitionChecksFailed" , "Pretransition checks failed"),
    CIM_DATE_RECEIVE("DeviceCimReceivedDate" , "Shipment date"),
    CIM_DATE_INSTALLED("DeviceCimInstalledDate" , "Installation date"),
    CIM_DATE_REMOVE("DeviceCimRemovedDate" , "Deactivation date"),
    CIM_DATE_RETRIED("DeviceCimRetriedDate" , "Decommissioning date"),
    LAST_CHECKED_PROPERTY_NAME(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key() , "Action effective timestamp"),
    ;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String formatWith(Thesaurus thesaurus, Object... arguments){
        Objects.requireNonNull(thesaurus);
        String translated = thesaurus.getString(getKey(), getDefaultFormat());
        return new MessageFormat(translated).format(arguments, new StringBuffer(), null).toString();
    }

    public String translateWith(Thesaurus thesaurus){
        return thesaurus.getString(getKey(), getDefaultFormat());
    }
}
