package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.prepayment.impl.BreakerStatus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * @author sva
 * @since 30/03/2016 - 15:39
 */
public class ContactorOperationDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        MRID_USAGE_POINT("mRIDUsagePoint", "mrid_usage_point"),
        MRID_DEVICE("mRIDDevice", "mrid_device"),
        BREAKER_STATUS("breakerStatus", "breaker_status"),
        ACTIVATION_DATE("activationDate", "activation_date"),
        NR_OF_UNCONFIRMED_DEVICE_COMMANDS("nrOfUnconfirmedDeviceCommands", "unconfirmed_commands"),
        STATUS_INFORMATION_TASK_TRY_COUNT("statusInformationTaskTryCount", "status_info_try_count"),
        CALLBACK("callback", "callback");

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

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String mRIDDevice;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String mRIDUsagePoint;
    private BreakerStatus breakerStatus;
    private Instant activationDate;
    private int nrOfUnconfirmedDeviceCommands;
    private int statusInformationTaskTryCount;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callback;

    public ContactorOperationDomainExtension() {
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

    public String getmRIDUsagePoint() {
        return mRIDUsagePoint;
    }

    public void setmRIDUsagePoint(String mRIDUsagePoint) {
        this.mRIDUsagePoint = mRIDUsagePoint;
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
        this.setmRIDUsagePoint((String) propertyValues.getProperty(FieldNames.MRID_USAGE_POINT.javaName()));
        this.setActivationDate((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setBreakerStatus((BreakerStatus) propertyValues.getProperty(FieldNames.BREAKER_STATUS.javaName()));
        this.setNrOfUnconfirmedDeviceCommands((Integer) propertyValues.getProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName()));
        this.setStatusInformationTaskTryCount((Integer) propertyValues.getProperty(FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName()));
        this.setCallback((String) propertyValues.getProperty(FieldNames.CALLBACK.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.MRID_DEVICE.javaName(), this.getmRIDDevice());
        propertySetValues.setProperty(FieldNames.MRID_USAGE_POINT.javaName(), this.getmRIDUsagePoint());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.BREAKER_STATUS.javaName(), this.getBreakerStatus());
        propertySetValues.setProperty(FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName(), this.getNrOfUnconfirmedDeviceCommands());
        propertySetValues.setProperty(FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName(), this.getStatusInformationTaskTryCount());
        propertySetValues.setProperty(FieldNames.CALLBACK.javaName(), this.getCallback());
    }

    @Override
    public void validateDelete() {
        //TODO: maybe check if the ServiceCall is completed - open servicecalls should not be deletable?
    }
}