/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.Size;

public class SIMCardDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<Device> {

    public enum FieldNames {

        DOMAIN("device", "device"),
        ICCID("iccid", "iccid"),
        PROVIDER("provider", "provider"),
        ACTIVE_IMSI("activeIMSI", "active_imsi"),
        INACTIVE_IMSI_FIRST("inactiveIMSIFirst", "inactive_imsi_first"),
        INACTIVE_IMSI_SECOND("inactiveIMSISecond", "inactive_imsi_second"),
        INACTIVE_IMSI_THIRD("inactiveIMSIThird", "inactive_imsi_third"),
        BATCH_ID("batchID", "batch_id"),
        CARD_FORMAT("cardFormat", "card_format"),
        STATUS("status", "status"),

        ;

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<Device> device = Reference.empty();

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.CAN_NOT_BE_EMPTY)
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String iccid;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.CAN_NOT_BE_EMPTY)
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String provider;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String activeIMSI;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String inactiveIMSIFirst;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String inactiveIMSISecond;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String inactiveIMSIThird;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String batchID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String cardFormat;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = MessageSeeds.Keys.FIELD_TOO_LONG)
    private String status;

    public SIMCardDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getActiveIMSI() {
        return activeIMSI;
    }

    public void setActiveIMSI(String activeIMSI) {
        this.activeIMSI = activeIMSI;
    }

    public String getInactiveIMSIFirst() {
        return inactiveIMSIFirst;
    }

    public void setInactiveIMSIFirst(String inactiveIMSIFirst) {
        this.inactiveIMSIFirst = inactiveIMSIFirst;
    }

    public String getInactiveIMSISecond() {
        return inactiveIMSISecond;
    }

    public void setInactiveIMSISecond(String inactiveIMSISecond) {
        this.inactiveIMSISecond = inactiveIMSISecond;
    }

    public String getInactiveIMSIThird() {
        return inactiveIMSIThird;
    }

    public void setInactiveIMSIThird(String inactiveIMSIThird) {
        this.inactiveIMSIThird = inactiveIMSIThird;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getCardFormat() {
        return cardFormat;
    }

    public void setCardFormat(String cardFormat) {
        this.cardFormat = cardFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void copyFrom(Device device, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.device.set(device);
        setIccid((String) propertyValues.getProperty(FieldNames.ICCID.javaName()));
        setProvider((String) propertyValues.getProperty(FieldNames.PROVIDER.javaName()));
        setActiveIMSI((String) propertyValues.getProperty(FieldNames.ACTIVE_IMSI.javaName()));
        setInactiveIMSIFirst((String) propertyValues.getProperty(FieldNames.INACTIVE_IMSI_FIRST.javaName()));
        setInactiveIMSISecond((String) propertyValues.getProperty(FieldNames.INACTIVE_IMSI_SECOND.javaName()));
        setInactiveIMSIThird((String) propertyValues.getProperty(FieldNames.INACTIVE_IMSI_THIRD.javaName()));
        setBatchID((String) propertyValues.getProperty(FieldNames.BATCH_ID.javaName()));
        setCardFormat((String) propertyValues.getProperty(FieldNames.CARD_FORMAT.javaName()));
        setStatus((String) propertyValues.getProperty(FieldNames.STATUS.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.ICCID.javaName(), getIccid());
        propertySetValues.setProperty(FieldNames.PROVIDER.javaName(), getProvider());
        propertySetValues.setProperty(FieldNames.ACTIVE_IMSI.javaName(), getActiveIMSI());
        propertySetValues.setProperty(FieldNames.INACTIVE_IMSI_FIRST.javaName(), getInactiveIMSIFirst());
        propertySetValues.setProperty(FieldNames.INACTIVE_IMSI_SECOND.javaName(), getInactiveIMSISecond());
        propertySetValues.setProperty(FieldNames.INACTIVE_IMSI_THIRD.javaName(), getInactiveIMSIThird());
        propertySetValues.setProperty(FieldNames.BATCH_ID.javaName(), getBatchID());
        propertySetValues.setProperty(FieldNames.CARD_FORMAT.javaName(), getCardFormat());
        propertySetValues.setProperty(FieldNames.STATUS.javaName(), getStatus());
    }

    @Override
    public void validateDelete() {
    }
}