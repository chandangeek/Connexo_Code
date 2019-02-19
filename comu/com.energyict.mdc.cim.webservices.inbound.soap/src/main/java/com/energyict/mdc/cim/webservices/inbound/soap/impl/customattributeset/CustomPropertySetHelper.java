/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.CustomPropertySetInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.LoggerUtils;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CustomPropertySetHelper {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetHelper.class.getName());

    private final CustomPropertySetService customPropertySetService;
    private final LoggerUtils loggerUtils;
    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private Clock clock;

    @Inject
    public CustomPropertySetHelper(CustomPropertySetService customPropertySetService, Thesaurus thesaurus,
            MeterConfigFaultMessageFactory faultMessageFactory, Clock clock) {
        this.customPropertySetService = customPropertySetService;
        this.clock = clock;
        this.faultMessageFactory = faultMessageFactory;
        this.loggerUtils = getLoggerUtils(thesaurus, faultMessageFactory);
    }

    /**
     * Sets values for CustomPropertySets on specific device logging detailed error messages if possible
     *
     * @param device
     * @param customPropertySetsData
     *
     * @return
     */
    public List<FaultMessage> addCustomPropertySetsData(Device device,
            List<CustomPropertySetInfo> customPropertySetsData) {
        return addCustomPropertySetsData(device, customPropertySetsData, null);
    }

    /**
     * Sets values for CustomPropertySets on specific device logging detailed error messages if possible
     *
     * @param device
     * @param customPropertySetsData
     * @param serviceCall
     *            service call for logging purposes
     * @return
     */
    public List<FaultMessage> addCustomPropertySetsData(Device device,
            List<CustomPropertySetInfo> customPropertySetsData, ServiceCall serviceCall) {
        List<FaultMessage> allFaults = new ArrayList<>();
        for (CustomPropertySetInfo info : customPropertySetsData) {
            List<FaultMessage> faults = addCustomPropertySet(device, info, serviceCall);
            if (faults.isEmpty()) {
                loggerUtils.logInfo(serviceCall, MessageSeeds.ASSIGNED_VALUES_FOR_CUSTOM_ATTRIBUTE_SET, info.getId());
            } else {
                allFaults.addAll(faults);
            }
        }
        return allFaults;
    }

    /**
     * Sets values for CustomPropertySet on specific device logging detailed error messages if possible
     *
     * @param device
     * @param newCustomPropertySetInfo
     * @param serviceCall
     *            service call for logging purposes
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<FaultMessage> addCustomPropertySet(Device device, CustomPropertySetInfo newCustomPropertySetInfo,
            ServiceCall serviceCall) {
        FaultSituationHandler faultSituationHandler = new FaultSituationHandler(serviceCall, loggerUtils, faultMessageFactory);
        try {
            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = customPropertySetService
                    .findActiveCustomPropertySet(newCustomPropertySetInfo.getId());
            if (!registeredCustomPropertySet.isPresent()) {
                throw faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET, newCustomPropertySetInfo.getId()).get();
            }
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet = registeredCustomPropertySet
                    .get().getCustomPropertySet();


            AttributeUpdater attributeUpdater = new AttributeUpdater(faultSituationHandler, device, customPropertySet);
            if (customPropertySet.isVersioned()) {
                new VersionedCasHandler(device, customPropertySet, customPropertySetService, attributeUpdater, faultSituationHandler, clock).handleVersionedCas(newCustomPropertySetInfo);
            } else {
                CustomPropertySetValues customPropertySetValues = attributeUpdater.newCasValues(newCustomPropertySetInfo);
                if (!attributeUpdater.anyFaults()) {
                    customPropertySetService.setValuesFor(customPropertySet, device, customPropertySetValues);
                }
            }
        } catch (FaultMessage ex) {
            faultSituationHandler.logSevere(device, ex);
        } catch (Exception ex) {
            faultSituationHandler.logException(device, ex,
                    MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET, newCustomPropertySetInfo.getId());
        }
        return faultSituationHandler.faults();
    }

    LoggerUtils getLoggerUtils(Thesaurus thesaurus, MeterConfigFaultMessageFactory faultMessageFactory) {
        return new LoggerUtils(LOGGER, thesaurus, faultMessageFactory);
    }
}
