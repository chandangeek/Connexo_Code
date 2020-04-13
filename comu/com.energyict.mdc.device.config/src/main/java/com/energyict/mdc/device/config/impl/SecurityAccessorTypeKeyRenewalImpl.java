/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeKeyRenewal;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.protocol.DeviceMessage;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

public class SecurityAccessorTypeKeyRenewalImpl implements SecurityAccessorTypeKeyRenewal {

    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCTYPE("securityAccessorType"),
        NAME("name"),
        VALUE("value"),
        SECURITYACCESSORTYPEONDEVICETYPE("securityAccessorTypeOnDeviceType"),
        SERVICEKEY("serviceKey");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityAccessorType> securityAccessorType = Reference.empty();

    private Reference<SecurityAccessorTypeOnDeviceType> securityAccessorTypeOnDeviceType = Reference.empty();
    private boolean serviceKey = false;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_IS_REQUIRED + "}")
    private Reference<DeviceMessage> deviceMessage = ValueReference.absent();
    private PropertySpec propertySpec;
    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    private Object value;
    @Size(max= Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String stringValue = ""; // the string representation of the value
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private final Thesaurus thesaurus;
    private final DataModel dataModel;

    SecurityAccessorTypeKeyRenewalImpl init(DeviceType deviceType, SecurityAccessorType securityAccessorType) {
        this.deviceType.set(deviceType);
        this.securityAccessorType.set(securityAccessorType);
        return this;
    }

    @Inject
    SecurityAccessorTypeKeyRenewalImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setValue(String value) {
        this.stringValue = value;
    }

    @Override
    public Object getValue() {
        if (this.value == null) {
            this.value = getSpecification().getValueFactory().fromStringValue(stringValue);
        }
        return value;
    }

    @Override
    public  PropertySpec getSpecification() {
        if (this.propertySpec == null) {
            this.propertySpec = securityAccessorTypeOnDeviceType.get().getKeyRenewalDeviceMessageSpecification(this.serviceKey).get().getPropertySpec(name).orElse(null);
        }
        return propertySpec;
    }

    @Override
    public boolean isServiceKey() {
        return serviceKey;
    }

    @Override
    public void setServiceKey(boolean serviceKey) {
        this.serviceKey = serviceKey;
    }


    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected void save() {
        Save.UPDATE.save(this.dataModel, this, Save.Update.class);
    }

}
