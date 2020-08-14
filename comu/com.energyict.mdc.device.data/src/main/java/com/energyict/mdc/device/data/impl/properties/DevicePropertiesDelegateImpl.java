package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.cps.*;
import com.elster.jupiter.transaction.*;
import com.energyict.mdc.common.device.data.*;
import com.energyict.mdc.common.tasks.*;
import com.energyict.mdc.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.upl.*;
import com.energyict.mdc.upl.properties.*;
import org.osgi.service.component.annotations.*;

import javax.inject.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;

/**
 * Provides an immediate link to the device connection type properties
 *
 * Used in WakeUp mechanisms to set/get the relevant connection properties
 */

@Component(name = "com.energyict.mdc.upl.properties.device.properties.delegate", service = {DevicePropertiesDelegate.class}, immediate = true)
public class DevicePropertiesDelegateImpl implements DevicePropertiesDelegate{



    // Injection and OSGI
    private volatile com.elster.jupiter.cps.CustomPropertySetService  customPropertySetService;
    private volatile TransactionService transactionService;

    // For OSGi purposes
    public DevicePropertiesDelegateImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DevicePropertiesDelegateImpl(CustomPropertySetService customPropertySetService, TransactionService transactionService) {
        this.customPropertySetService = customPropertySetService;
        this.transactionService = transactionService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService  customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @Activate
    public void activate() {
        Services.devicePropertiesDelegate(this);
    }

    @Deactivate
    public void deactivate() {
        Services.devicePropertiesDelegate(null);
    }


    // ------------------ useful implementation


    @Override
    public boolean setConnectionMethodProperty(String propertyName, Object value, String customPropertySetClass, String deviceMRID, Long connectionTaskId) {
        // first find our device, to avoid duplicate go straight for the MRID

        Optional<com.energyict.mdc.upl.meterdata.Device> deviceUPL = findDeviceByMRID(deviceMRID);
        if (!deviceUPL.isPresent()){
            logError("Cannot find a device with MRID {"+deviceMRID+"}");
            return false;
        }
        // now find the connection task, also directly by ID
        // TODO: check if we can get the connectionTask by ID without the device, e.g. create a ConnectionTaskService
        // also we need to cast UPL to MDC "Device" class ... stupid architecture
        com.energyict.mdc.common.device.data.Device deviceMDC = (Device) deviceUPL.get();
        Optional<ConnectionTask<?, ?>> connectionProvider = findConnectionProvider(deviceMDC, connectionTaskId);

        // get the custom property set, using the properties class name
        if (!connectionProvider.isPresent()){
            logError("Cannot find a connection task with id "+connectionTaskId+" on device with MRID {"+deviceMRID+"}");
            return false;
        }

        // get the custom property set, using the properties class name
        Optional<CustomPropertySet> customPropertySet = getCustomPropertySet(customPropertySetClass);
        if (!customPropertySet.isPresent()) {
            logError("Cannot find registered custom property set for "+customPropertySetClass);
        }

        // get the current set of values (general of deviceType and customized ones)
        // if we don't do this, we'll end up with the defaults from all the others
        CustomPropertySetValues values;
        if (customPropertySet.get().isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet.get(), connectionProvider.get(), Instant.now());
        } else {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet.get(), connectionProvider.get());
        }

        // write the value we're trying to set
        values.setProperty(propertyName, value);

        // execute the writing in a transaction!
        try {
            transactionService.execute(() -> {
                if (customPropertySet.get().isVersioned()) {
                    customPropertySetService.setValuesFor(customPropertySet.get(), connectionProvider.get(), values, Instant.now());
                } else {
                    customPropertySetService.setValuesFor(customPropertySet.get(), connectionProvider.get(), values);
                }
                return null;
            });
        } catch (Exception ex){
            logError("Could not set the property of connection task "+connectionProvider.get().getName()
                    +" of device "+deviceMDC.getName()
                    +" with MRID {"+deviceMRID+"}"
                    +" because of: "+ex.getLocalizedMessage());
            return false;
        }
        // great success!
        return true;
    }

    @Override
    public Optional<Map<String, Object>> getConnectionMethodProperties(String customPropertySetClass, String deviceMRID, Long connectionTaskId){

        // first find our device, to avoid duplicate go straight for the MRID
        Optional<com.energyict.mdc.upl.meterdata.Device> deviceUPL = findDeviceByMRID(deviceMRID);
        if (!deviceUPL.isPresent()){
            logError("Cannot find a device with MRID {"+deviceMRID+"}");
            return Optional.empty();
        }

        // now find the connection task, also directly by ID
        // TODO: check if we can get the connectionTask by ID without the device, e.g. create a ConnectionTaskService
        // also we need to cast UPL to MDC "Device" class ... stupid architecture
        com.energyict.mdc.common.device.data.Device deviceMDC = (Device) deviceUPL.get();
        Optional<ConnectionTask<?, ?>> connectionProvider = findConnectionProvider(deviceMDC, connectionTaskId);
        if (!connectionProvider.isPresent()){
            logError("Cannot find a connection task with id "+connectionTaskId+" on device with MRID {"+deviceMRID+"}");
            return Optional.empty();
        }

        // get the custom property set, using the properties class name
        Optional<CustomPropertySet> customPropertySet = getCustomPropertySet(customPropertySetClass);
        if (!customPropertySet.isPresent()){
            logError("Cannot find registered custom property set for "+customPropertySetClass);
            return Optional.empty();
        }

        // finally get the values
        CustomPropertySetValues valuesSet;
        if (customPropertySet.get().isVersioned()) {
            valuesSet = customPropertySetService
                    .getUniqueValuesFor(customPropertySet.get(), connectionProvider.get(), Instant.now());
        } else {
            valuesSet = customPropertySetService
                    .getUniqueValuesFor(customPropertySet.get(), connectionProvider.get());
        }
        Map<String, Object> allResults = new HashMap<>();

        valuesSet.propertyNames()
                .stream()
                .forEach( propName -> allResults.put(propName, valuesSet.getProperty(propName)));

        // great success
        return Optional.of(allResults);
    }


    private Optional<ConnectionTask<?, ?>> findConnectionProvider(Device device, Long connectionTaskId){
        return device.getConnectionTasks()
                .stream()
                .filter(ct -> ct.getId() == connectionTaskId)
                .findAny();
    }

    private Optional<CustomPropertySet> getCustomPropertySet(String customPropertySetClass){
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = findRegisteredCustomPropertySet(customPropertySetClass);
        if (registeredCustomPropertySet.isPresent()){
            return Optional.of(registeredCustomPropertySet.get().getCustomPropertySet());
        }
        return Optional.empty();
    }

    private Optional<com.energyict.mdc.upl.meterdata.Device> findDeviceByMRID(String mrID){
        return Services.deviceFinder().find(new DeviceIdentifierByMRID(mrID));
    }


    private Optional<com.energyict.mdc.upl.meterdata.Device> findDeviceBySerialNumber(String serialNumber) {
        return Services.deviceFinder().find(new DeviceIdentifierBySerialNumber(serialNumber));
    }

    private Optional<RegisteredCustomPropertySet> findRegisteredCustomPropertySet(String customPropertySetClass){
        return customPropertySetService.findActiveCustomPropertySet(customPropertySetClass);
    }

    private void logError(String errorMessage) {
        Logger.getLogger(this.getClass().getName()).severe(errorMessage);
    }

}