package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.config.DeviceType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/*XROMVYU*/
public class DeviceTypeTypeDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<DeviceType> {

    public enum FieldNames {
        DOMAIN("deviceType", "deviceType"),
        MANUFACT_NAME_STRING("manufacturerName", "manufacturer_name"),
        MANUFACT_ID_NUMBER("manufacturerId", "manufacturer_id");

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

    private Reference<DeviceType> deviceType = Reference.empty();


    @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong"/*"{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}"*/)
    private String manufacturerName;
    @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
    private BigDecimal manufacturerId;


    public void setManufacturerName(String manufacturerName){
        this.manufacturerName = manufacturerName;

    }

    public void setManufacturerId(BigDecimal manufacturerId){
        this.manufacturerId = manufacturerId;
    }


    public String getManufacturerName(){
        return manufacturerName;
    }

    public BigDecimal getManufacturerId(){
        return manufacturerId;
    }

    @Override
    public void copyFrom(DeviceType deviceType, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.deviceType.set(deviceType);
        this.setManufacturerId(new BigDecimal(propertyValues.getProperty(FieldNames.MANUFACT_ID_NUMBER.javaName()).toString()));
        this.setManufacturerName((String) propertyValues.getProperty(FieldNames.MANUFACT_NAME_STRING.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.MANUFACT_ID_NUMBER.javaName(), this.getManufacturerId());
        propertySetValues.setProperty(FieldNames.MANUFACT_NAME_STRING.javaName(), this.getManufacturerName());
    }

    @Override
    public void validateDelete() {
    }

}
