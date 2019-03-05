package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttributeUpdater {
    private FaultSituationHandler exceptionHandler;
    private Device device;
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;

    public AttributeUpdater(FaultSituationHandler exceptionHandler, Device device, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet){

        this.exceptionHandler = exceptionHandler;
        this.device = device;
        this.customPropertySet = customPropertySet;
    }

    public boolean anyFaults(){
        return exceptionHandler.anyException();
    }

    public CustomPropertySetValues newCasValues(CasInfo newCasInfo) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        updateCasValues(newCasInfo, values);
        return values;
    }

    public void updateCasValues(CasInfo newCasInfo, CustomPropertySetValues values) {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (Map.Entry<String, String> newAttributeNameAndValue : newCasInfo.getAttributes().entrySet()) {
            String attributeName = newAttributeNameAndValue.getKey();
            Optional<PropertySpec> propertySpec = propertySpecs.stream()
                    .filter(spec -> spec.getName().equals(attributeName)).findAny();
            if (propertySpec.isPresent()) {
                setAttributeValue(device, newCasInfo, values,
                        newAttributeNameAndValue, propertySpec.get());
            } else {
                exceptionHandler.logSevere(device, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE,
                        attributeName, newCasInfo.getId());
            }
        }
    }

    private void setAttributeValue(Device device, CasInfo info, CustomPropertySetValues values, Map.Entry<String, String> attributeEntry,
                                   PropertySpec propertySpec) {
        Object fromStringValue;
        try {
            fromStringValue = propertySpec.getValueFactory().fromStringValue(attributeEntry.getValue());
            values.setProperty(attributeEntry.getKey(), fromStringValue);
        } catch (Exception ex) {
            exceptionHandler.logException(device, ex,
                    MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE, attributeEntry.getValue(),
                    attributeEntry.getKey(), info.getId());
        }
    }

    public List<FaultMessage> faults() {
        return exceptionHandler.faults();
    }
}
