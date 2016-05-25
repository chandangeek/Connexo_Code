package com.energyict.mdc.device.data.ami.servicecalls;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.ami.BreakerStatus;

import javax.validation.constraints.Size;
import java.time.Instant;

public class ServiceOperationDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        MRID_DEVICE("mRIDDevice", "mrid_device"),
        BREAKER_STATUS("breakerStatus", "breaker_status"),
        ACTIVATION_DATE("activationDate", "activation_date"),
        READING_TYPE("readingType","reading_type"),
        NR_OF_UNCONFIRMED_DEVICE_COMMANDS("nrOfUnconfirmedDeviceCommands", "unconfirmed_commands"),
        STATUS_INFORMATION_TASK_TRY_COUNT("statusInformationTaskTryCount", "status_info_try_count"),
        CALLBACK("callback", "callback"),
        LOAD_LIMIT_ENABLED ("loadLimitEnabled","load_limit_enabled"),
        LOAD_LIMIT ("loadLimit","loadLimit"),
        DESTINATION_SPEC_NAME("destinationSpecName", "destination_spec_name"),
        COMPLETION_MESSAGE("completionMessage","completion_message");

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
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.device.data.impl.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String mRIDDevice;
    private BreakerStatus breakerStatus;
    private Instant activationDate;
    public String readingType;
    private int nrOfUnconfirmedDeviceCommands;
    private int statusInformationTaskTryCount;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.device.data.impl.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callback;
    private Boolean loadLimitEnabled;
    private Quantity loadLimit;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.device.data.impl.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String destinationSpecName;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.energyict.mdc.device.data.impl.MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String completionMessage;

    public ServiceOperationDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getmRIDDevice() {
        return mRIDDevice;
    }

    public void setmRIDDevice(String mRIDDevice) {
        this.mRIDDevice = mRIDDevice;
    }

    public BreakerStatus getBreakerStatus() {
        return breakerStatus;
    }

    public void setBreakerStatus(BreakerStatus breakerStatus) {
        this.breakerStatus = breakerStatus;
    }

    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    public String getDestinationSpecName() {
        return destinationSpecName;
    }

    public void setDestinationSpecName(String destinationSpecName) {
        this.destinationSpecName = destinationSpecName;
    }

    public String getCompletionMessage() {
        return completionMessage;
    }

    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }

    public String getReadingType() {
        return readingType;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }

    public Boolean getLoadLimitEnabled() {
        return loadLimitEnabled;
    }

    public void setLoadLimitEnabled(Boolean loadLimitEnabled) {
        this.loadLimitEnabled = loadLimitEnabled;
    }

    public Quantity getLoadLimit() {
        return loadLimit;
    }

    public void setLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
    }

    public int getNrOfUnconfirmedDeviceCommands() {
        return nrOfUnconfirmedDeviceCommands;
    }

    public void setNrOfUnconfirmedDeviceCommands(int nrOfUnconfirmedDeviceCommands) {
        this.nrOfUnconfirmedDeviceCommands = nrOfUnconfirmedDeviceCommands;
    }

    public int getStatusInformationTaskTryCount() {
        return statusInformationTaskTryCount;
    }

    public void setStatusInformationTaskTryCount(int statusInformationTaskTryCount) {
        this.statusInformationTaskTryCount = statusInformationTaskTryCount;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setmRIDDevice((String) propertyValues.getProperty(FieldNames.MRID_DEVICE.javaName()));
        this.setActivationDate((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setBreakerStatus((BreakerStatus) propertyValues.getProperty(FieldNames.BREAKER_STATUS.javaName()));
        this.setReadingType((String) propertyValues.getProperty(FieldNames.READING_TYPE.javaName()));
        this.setNrOfUnconfirmedDeviceCommands((Integer) propertyValues.getProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName()));
        this.setStatusInformationTaskTryCount((Integer) propertyValues.getProperty(FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName()));
        this.setCallback((String) propertyValues.getProperty(FieldNames.CALLBACK.javaName()));
        this.setLoadLimitEnabled((Boolean) propertyValues.getProperty(FieldNames.LOAD_LIMIT_ENABLED.javaName()));
        this.setLoadLimit((Quantity) propertyValues.getProperty(FieldNames.LOAD_LIMIT.javaName()));
        this.setDestinationSpecName((String) propertyValues.getProperty(FieldNames.DESTINATION_SPEC_NAME.javaName()));
        this.setCompletionMessage((String) propertyValues.getProperty(FieldNames.COMPLETION_MESSAGE.javaName()));

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.MRID_DEVICE.javaName(), this.getmRIDDevice());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.BREAKER_STATUS.javaName(), this.getBreakerStatus());
        propertySetValues.setProperty(FieldNames.READING_TYPE.javaName(), this.getReadingType());
        propertySetValues.setProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName(), this.getNrOfUnconfirmedDeviceCommands());
        propertySetValues.setProperty(FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName(), this.getStatusInformationTaskTryCount());
        propertySetValues.setProperty(FieldNames.CALLBACK.javaName(), this.getCallback());
        propertySetValues.setProperty(FieldNames.LOAD_LIMIT_ENABLED.javaName(), this.getLoadLimitEnabled());
        propertySetValues.setProperty(FieldNames.LOAD_LIMIT.javaName(), this.getLoadLimit());
        propertySetValues.setProperty(FieldNames.DESTINATION_SPEC_NAME.javaName(), this.getDestinationSpecName());
        propertySetValues.setProperty(FieldNames.COMPLETION_MESSAGE.javaName(), this.getCompletionMessage());
    }

    @Override
    public void validateDelete() {
        //TODO: maybe check if the ServiceCall is completed - open servicecalls should not be deletable?
    }
}
