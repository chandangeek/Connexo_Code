/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 03/06/2016 - 15:39
 */
public class CommandServiceCallDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        RELEASE_DATE("releaseDate", "release_date"),
        DEVICE_MSG("deviceMessages", "device_msg"),
        NR_OF_UNCONFIRMED_DEVICE_COMMANDS("nrOfUnconfirmedDeviceCommands", "unconfirmed_commands"),
        STATUS("status", "status");

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

    private Reference<ServiceCall> serviceCall = Reference.empty();

    private Instant releaseDate;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceMessages;
    private int nrOfUnconfirmedDeviceCommands;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String status;

    public CommandServiceCallDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDeviceMessages() {
        return deviceMessages;
    }

    public void setDeviceMessages(String deviceMessages) {
        this.deviceMessages = deviceMessages;
    }

    public void setDeviceMessages(List<DeviceMessage<Device>> deviceMessages) {
        this.deviceMessages = Arrays.toString(deviceMessages.stream().map(deviceMessage -> Long.toString(deviceMessage.getId())).toArray());
    }

    public int getNrOfUnconfirmedDeviceCommands() {
        return nrOfUnconfirmedDeviceCommands;
    }

    public void setNrOfUnconfirmedDeviceCommands(int nrOfUnconfirmedDeviceCommands) {
        this.nrOfUnconfirmedDeviceCommands = nrOfUnconfirmedDeviceCommands;
    }

    public CommandOperationStatus getCommandOperationStatus() {
        return CommandOperationStatus.fromName(status);
    }

    public void setCommandOperationStatus(CommandOperationStatus commandOperationStatus) {
        this.status = commandOperationStatus.name();
    }

    private String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setReleaseDate((Instant) propertyValues.getProperty(FieldNames.RELEASE_DATE.javaName()));
        this.setDeviceMessages((String) propertyValues.getProperty(FieldNames.DEVICE_MSG.javaName));
        this.setNrOfUnconfirmedDeviceCommands((Integer) propertyValues.getProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName()));
        this.setStatus((String) propertyValues.getProperty(FieldNames.STATUS.javaName));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.RELEASE_DATE.javaName(), this.getReleaseDate());
        propertySetValues.setProperty(FieldNames.DEVICE_MSG.javaName(), this.getDeviceMessages());
        propertySetValues.setProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName(), this.getNrOfUnconfirmedDeviceCommands());
        propertySetValues.setProperty(FieldNames.STATUS.javaName(), this.getStatus());
    }

    @Override
    public void validateDelete() {
    }
}