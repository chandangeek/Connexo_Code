package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public class GetMeterReadingsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        /// TODO check what ever fields are needed (isRegular and so on)
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        END_DEVICE_MRID("endDeviceMRID", "endDeviceMRID"),
        END_DEVICE_NAME("endeDeviceName", "endeDeviceName"),
        CHANNELS("channels", "channels"),
        REGISTERS("registers", "registers");
//        START_DATE("startDate","startDate"); //when com task was started

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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endDeviceMRID;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endeDeviceName;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String channels;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String registers;

    public GetMeterReadingsDomainExtension() {
        super();
    }

    public BigDecimal getParentServiceCallId() {
        return parentServiceCallId;
    }

    public void setParentServiceCallId(BigDecimal parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public String getEndDeviceMRID() {
        return endDeviceMRID;
    }

    public void setEndDeviceMRID(String endDeviceMRID) {
        this.endDeviceMRID = endDeviceMRID;
    }

    public String getEndeDeviceName() {
        return endeDeviceName;
    }

    public void setEndeDeviceName(String endeDeviceName) {
        this.endeDeviceName = endeDeviceName;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getRegisters() {
        return registers;
    }

    public void setRegisters(String registers) {
        this.registers = registers;
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public void setServiceCall(Reference<ServiceCall> serviceCall) {
        this.serviceCall = serviceCall;
    }


    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setParentServiceCallId((BigDecimal) propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()));
        this.setEndDeviceMRID((String) propertyValues.getProperty(FieldNames.END_DEVICE_MRID.javaName()));
        this.setEndeDeviceName((String) propertyValues.getProperty(FieldNames.END_DEVICE_NAME.javaName()));
        this.setChannels((String) propertyValues.getProperty(FieldNames.CHANNELS.javaName()));
        this.setRegisters((String) propertyValues.getProperty(FieldNames.REGISTERS.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.END_DEVICE_MRID.javaName(), this.getEndDeviceMRID());
        propertySetValues.setProperty(FieldNames.END_DEVICE_NAME.javaName(), this.getEndeDeviceName());
        propertySetValues.setProperty(FieldNames.REGISTERS.javaName(), this.getRegisters());
        propertySetValues.setProperty(FieldNames.CHANNELS.javaName(), this.getChannels());
    }

    @Override
    public void validateDelete() {
    }
}
