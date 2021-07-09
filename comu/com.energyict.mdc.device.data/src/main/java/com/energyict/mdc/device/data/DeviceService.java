/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:27)
 */
@ProviderType
public interface DeviceService {

    /**
     * Creates a new Device based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param startDate           the meter activation's start date
     * @return the newly created Device
     */
    Device newDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name, Instant startDate);

    /**
     * Creates a new Device based on the given name, DeviceConfiguration and batch.
     * If batch with specified name doesn't exist then new one will be created.
     * The device will have a {@link com.elster.jupiter.metering.MeterActivation} starting at the create time
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param batch               the name of batch
     * @param startDate           the start date of the {@link com.elster.jupiter.metering.MeterActivation}
     * @return the newly created Device
     */
    Device newDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name, String batch, Instant startDate);

    /**
     * Creates a new Device builder based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param startDate           the meter activation's start date
     * @return the newly created Device builder
     */
    DeviceBuilder newDeviceBuilder(DeviceConfiguration deviceConfiguration, String name, Instant startDate);

    /**
     * Finds the Device based on his unique ID.
     *
     * @param id the unique ID of the device
     * @return the requested Device or null if none was found
     */
    Optional<Device> findDeviceById(long id);

    Optional<Device> findDeviceByIdentifier(DeviceIdentifier identifier);

    List<Device> findAllDevicesByIdentifier(DeviceIdentifier identifier);

    /**
     * Finds and locks the Device based on his unique ID and VERSION.
     *
     * @param id      the unique ID of the device
     * @param version the version of the device
     * @return the requested Device or null if none was found
     */
    Optional<Device> findAndLockDeviceByIdAndVersion(long id, long version);

    Optional<Device> findAndLockDeviceById(long id);

    Optional<Device> findAndLockDeviceByNameAndVersion(String name, long version);

    Optional<Device> findAndLockDeviceBymRIDAndVersion(String mRID, long version);

    Optional<SecurityAccessor<SecurityValueWrapper>> findAndLockSecurityAccessorById(Device device, SecurityAccessorType securityAccessorType);

    /**
     * If the security accessor identified by device & security accessor type exists AND has the expected version, the corresponding
     * security accessor will be returned
     * @param device Part of KeyAccessor primary key
     * @param securityAccessorType Part of KeyAccessor primary key
     * @param version The expected version
     */
    Optional<SecurityAccessor<SecurityValueWrapper>> findAndLockKeyAccessorByIdAndVersion(Device device, SecurityAccessorType securityAccessorType, long version);

    /**
     * Finds the Device based on his unique internal name
     *
     * @param name the unique identifier of the device
     * @return the requested Device
     */
    Optional<Device> findDeviceByName(String name);

    /**
     * Finds the Device based on his mRID which is semantically a (Java-formatted) UUID as specified in RFC 4122.
     *
     * @param mrId String containing a (Java-formatted) UUID as specified in RFC 4122.
     * @return {@link Optional} of the requested Device or empty if none was found.
     */
    Optional<Device> findDeviceByMrid(String mrId);

    /**
     * Finds the Device based on the METERID.
     *
     * @param meterId the METERID of the device
     * @return the requested Device or null if none was found
     */
    Optional<Device> findDeviceByMeterId(long meterId);

    /**
     * Finds the Devices (multiple are possible) based on the given serialNumber.
     *
     * @param serialNumber the serialNumber of the device
     * @return a list of Devices which have the given serialNumber
     */
    List<Device> findDevicesBySerialNumber(String serialNumber);

    /**
     * Finds all the devices in the system with the specific condition.
     *
     * @return a list of all devices with the specific condition in the system
     */
    Finder<Device> findAllDevices(Condition condition);

    /**
     * Returns true if the ComSchedule has been linked to a device.
     */
    boolean isLinkedToDevices(ComSchedule comSchedule);

    Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    /**
     * Finds all devices which have a property (general Attribute) with the given name and value.
     *
     * @param propertySpecName  the name of the property
     * @param propertySpecValue the value of the property
     * @return a list of all devices matching the given criteria
     */
    List<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue);

    /**
     * Finds all devices which have a connectionTask of the given ConnectionType and a property matching the given arguments.
     *
     * @param connectionTypeClass the type of the connectionTask
     * @param propertyName        the name of the property
     * @param propertyValue       the value of the property
     * @return a list of all devices matching the given criteria
     */
    List<Device> findDevicesByConnectionTypeAndProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue);

    Query<Device> deviceQuery();

    /**
     * Change the DeviceConfiguration of the device to the provided destinationDeviceConfiguration.
     * <b>NOTE:</b> Make sure you don't create your own transaction. This will be performed during the execution
     * of this method. Multiple transactions are required to perform Business Locks.
     *
     * @param deviceId
     * @param deviceVersion
     * @param destinationDeviceConfigId      the ID fo the DestinationDeviceConfig
     * @param destinationDeviceConfigVersion the version to check   @return the given device with the new configuration applied
     */
    Device changeDeviceConfigurationForSingleDevice(long deviceId, long deviceVersion, long destinationDeviceConfigId, long destinationDeviceConfigVersion);

    /**
     * Change the DeviceConfiguration for the given set of Devices to the provided destinationDeviceConfiguration.
     * The action will be queued and the processing is asynchronously.
     *
     * @param destinationDeviceConfiguration the configuration which should be applied
     * @param devicesForConfigChangeSearch
     * @param deviceIds                    a list of device IDs
     */
    void changeDeviceConfigurationForDevices(DeviceConfiguration destinationDeviceConfiguration, DevicesForConfigChangeSearch devicesForConfigChangeSearch, Long... deviceIds);

    Optional<ActivatedBreakerStatus> getActiveBreakerStatus(Device device);

    ActivatedBreakerStatus newActivatedBreakerStatusFrom(Device device, BreakerStatus collectedBreakerStatus, Interval interval);

    Optional<CreditAmount> getCreditAmount(Device device);

    Optional<CreditAmount> getCreditAmount(Device device, Instant instant);

    CreditAmount creditAmountFrom(Device device, String collectedCreditType, BigDecimal collectedCreditAmount);

    List<Device> findActiveValidatedDevices(List<Device> domainObjects);

    /**
     * Deletes all outdated {@link ComTaskExecutionTrigger}s<br/>
     * More specific, all ComTaskExecutionTriggers who have a trigger date more than 1 day in the past will be deleted
     */
    void deleteOutdatedComTaskExecutionTriggers();

    void transferActiveBreakerStatusToDb(Device device, BreakerStatus breakerStatus);

    /**
     * Returns true if the provided {@link CertificateWrapper} is still referenced by a KeyAccessor
     * @param certificate The to-be-checked for usage {@link CertificateWrapper}
     * @return true if in use, false otherwise
     */
    boolean usedByKeyAccessor(CertificateWrapper certificate);

    /**
      * Returns collection of {@link SecurityAccessor} objects used by the given {@link CertificateWrapper} object
      * @param certificate The {@link CertificateWrapper} object
      * @return Collection of associated {@link SecurityAccessor} objects
      */
    List<SecurityAccessor> getAssociatedKeyAccessors(CertificateWrapper certificate);
}
