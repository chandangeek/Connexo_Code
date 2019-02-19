package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.CustomPropertySetInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.FaultSituationHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class VersionedCasHandler {
    private Device device;
    private final AttributeUpdater attributeUpdater;
    private CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet;
    private CustomPropertySetService customPropertySetService;
    private CASConflictsSolver casConflictsSolver;
    private FaultSituationHandler faultSituationHandler;
    private Clock clock;

    public VersionedCasHandler(Device device, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet, CustomPropertySetService customPropertySetService,
                               AttributeUpdater attributeUpdater,
                               FaultSituationHandler faultSituationHandler, Clock clock) {
        this.device = device;
        this.attributeUpdater = attributeUpdater;
        this.customPropertySet = customPropertySet;
        this.customPropertySetService = customPropertySetService;
        this.casConflictsSolver = new CASConflictsSolver(customPropertySetService);
        this.faultSituationHandler = faultSituationHandler;
        this.clock = clock;
    }

    private boolean fromDateBeforeDevice(Device device, Instant fromDate) {
        return device.getLifecycleDates().getReceivedDate().isPresent() &&
                fromDate.truncatedTo(ChronoUnit.DAYS).isBefore(device.getLifecycleDates().getReceivedDate().get().truncatedTo(ChronoUnit.DAYS));
    }

    public void handleVersionedCas(CustomPropertySetInfo newCustomPropertySetInfo) throws FaultMessage {

        if (newCustomPropertySetInfo.getVersionId() == null) {
            createNewVersion(newCustomPropertySetInfo);
        } else {
            updateExistingVersion(newCustomPropertySetInfo);
        }
    }

    private void createNewVersion(CustomPropertySetInfo newCustomProperySetInfo) throws FaultMessage {
        CustomPropertySetValues values = attributeUpdater.newCasValues(newCustomProperySetInfo);
        if(attributeUpdater.anyFaults()){
            return;
        }
        Instant fromDate = newCustomProperySetInfo.getFromDate();
        if (fromDate == null || fromDateBeforeDevice(device, fromDate)) {
            throw faultSituationHandler.newFault(device.getName(), MessageSeeds.START_DATE_LOWER_CREATED_DATE,
                    device.getName());
        }
        Range<Instant> range = casConflictsSolver.solveConflictsForCreate(device, customPropertySet,
                fromDate, newCustomProperySetInfo.getEndDate());
        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range);
    }

    private void updateExistingVersion(CustomPropertySetInfo newCustomPropertySetInfo) throws FaultMessage {
        Optional<Instant> startTime = Optional.ofNullable(newCustomPropertySetInfo.getFromDate());
        Optional<Instant> endTime = Optional.ofNullable(newCustomPropertySetInfo.getEndDate());
        Instant versionId = newCustomPropertySetInfo.getVersionId();
        CustomPropertySetValues existingValues = customPropertySetService.getUniqueValuesFor(customPropertySet, device,
                versionId);
        if (existingValues.isEmpty()) {
            throw faultSituationHandler.newFault(device.getName(), MessageSeeds.NO_CUSTOM_ATTRIBUTE_VERSION,
                    DefaultDateTimeFormatters.shortDate().withShortTime().build()
                            .format(versionId.atZone(clock.getZone())));
        } else {
            attributeUpdater.updateCasValues(newCustomPropertySetInfo, existingValues);
            if(attributeUpdater.anyFaults()){
                return;
            }
            if(newCustomPropertySetInfo.isUpdateRange()) {
                if(!startTime.isPresent()){
                    startTime = Optional.of(device.getCreateTime());
                }
                if (!endTime.isPresent()) {
                    endTime = Optional.of(Instant.EPOCH);
                }
                Range<Instant> range = casConflictsSolver.solveConflictsForUpdate(device, customPropertySet, startTime,
                        endTime, versionId, existingValues);
                customPropertySetService.setValuesVersionFor(customPropertySet, device, existingValues, range, versionId);
            }else{
                customPropertySetService.setValuesVersionFor(customPropertySet, device, existingValues, existingValues.getEffectiveRange(), versionId);
            }
        }
    }
}
