/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ScheduleStrategy;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SubMasterEndDeviceControlsDomainExtension extends AbstractPersistentDomainExtension
        implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        COMMAND_CODE("commandCode", "COMMAND_CODE"),
        COMMAND_ATTRIBUTES("commandAttributes", "COMMAND_ATTRIBUTES"),
        SCHEDULE_STRATEGY("scheduleStrategy", "SCHEDULE_STRATEGY");

        private final String javaName;
        private final String databaseName;

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String commandCode;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String commandAttributes;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String scheduleStrategy;

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    public String getCommandAttributes() {
        return commandAttributes;
    }

    public void setCommandAttributes(String commandAttributes) {
        this.commandAttributes = commandAttributes;
    }

    public String getScheduleStrategy() {
        return scheduleStrategy;
    }

    public boolean hasRunWithPriorityStrategy() {
        return ScheduleStrategy.RUN_WITH_PRIORITY.getName().equals(scheduleStrategy);
    }

    public void setScheduleStrategy(String scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setCommandCode((String) propertyValues.getProperty(FieldNames.COMMAND_CODE.javaName()));
        this.setCommandAttributes((String) propertyValues.getProperty(FieldNames.COMMAND_ATTRIBUTES.javaName()));
        this.setScheduleStrategy((String) propertyValues.getProperty(FieldNames.SCHEDULE_STRATEGY.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.COMMAND_CODE.javaName(), this.getCommandCode());
        propertySetValues.setProperty(FieldNames.COMMAND_ATTRIBUTES.javaName(), this.getCommandAttributes());
        propertySetValues.setProperty(FieldNames.SCHEDULE_STRATEGY.javaName(), this.getScheduleStrategy());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}
