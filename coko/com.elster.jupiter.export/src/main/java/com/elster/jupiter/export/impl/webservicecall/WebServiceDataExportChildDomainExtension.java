package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.impl.MessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WebServiceDataExportChildDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        DATA_SOURCE_ID("dataSourceId", "DATA_SOURCE_ID"),
        DEVICE_NAME("deviceName", "DEVICE_NAME"),
        READING_TYPE_MRID("readingTypeMRID", "READING_TYPE_MRID"),
        CUSTOM_INFO("customInfo", "CUSTOM_INFO");

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

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + '}')
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + '}')
    private String deviceName;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + '}')
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + '}')
    private String readingTypeMRID;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + '}')
    private String customInfo;

    private long dataSourceId;

    public WebServiceDataExportChildDomainExtension() {
        super();
    }

    public String getDeviceName(){
        return this.deviceName;
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public String getReadingTypeMRID(){
        return this.readingTypeMRID;
    }

    public void setReadingTypeMRID(String readingTypeMRID){
        this.readingTypeMRID = readingTypeMRID;
    }

    public long getDataSourceId(){
        return this.dataSourceId;
    }
    public void setDataSourceId(long dataSourceId){
        this.dataSourceId = dataSourceId;
    }

    public String getCustomInfo(){
        return this.customInfo;
    }

    public void setCustomInfo(String customInfo){
        this.customInfo = customInfo;
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        setDeviceName((String) propertyValues.getProperty(FieldNames.DEVICE_NAME.javaName()));
        setReadingTypeMRID((String) propertyValues.getProperty(FieldNames.READING_TYPE_MRID.javaName()));
        setDataSourceId((long) propertyValues.getProperty(FieldNames.DATA_SOURCE_ID.javaName()));
        setCustomInfo((String) propertyValues.getProperty(FieldNames.CUSTOM_INFO.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_NAME.javaName(), getDeviceName());
        propertySetValues.setProperty(FieldNames.READING_TYPE_MRID.javaName(), getReadingTypeMRID());
        propertySetValues.setProperty(FieldNames.DATA_SOURCE_ID.javaName(), getDataSourceId());
        propertySetValues.setProperty(FieldNames.CUSTOM_INFO.javaName(), getCustomInfo());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}
