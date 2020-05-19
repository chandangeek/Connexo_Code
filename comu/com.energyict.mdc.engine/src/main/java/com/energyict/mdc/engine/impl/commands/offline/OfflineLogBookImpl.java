/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineLogBookSpec;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Represents an Offline version of a {@link com.energyict.mdc.upl.meterdata.LogBook}
 *
 * @author sva
 * @since 07/12/12 - 14:36
 */
public class OfflineLogBookImpl implements OfflineLogBook {

    /**
     * The {@link com.energyict.mdc.upl.meterdata.LogBook} which is going offline
     */
    private LogBook logBook;
    private Device device;
    private DeviceIdentifier deviceIdentifier;
    private IdentificationService identificationService;
    private LogBookIdentifier logBookIdentifier;

    /**
     * The {@link OfflineLogBookSpec} for the LogBookType.
     */
    private OfflineLogBookSpec offlineLogBookSpec;
    /**
     * The database ID of this {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice devices'} {@link com.energyict.mdc.upl.meterdata.LogBook}
     */
    private long logBookId;

    /**
     * The id of the {@link com.energyict.mdc.upl.meterdata.Device} which owns this LogBook.
     */
    private int deviceId;

    /**
     * The serialNumber of the {@link com.energyict.mdc.upl.meterdata.Device Device}
     */
    private String serialNumber;
    /**
     * The Date from where to start fetching data from the {@link com.energyict.mdc.upl.meterdata.LogBook}
     */
    private Date lastReading;

    public OfflineLogBookImpl() {
        super();
    }

    public OfflineLogBookImpl(LogBook logBook, IdentificationService identificationService) {
        this.logBook = logBook;
        this.identificationService = identificationService;
        this.device = logBook.getDevice();
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setLogBookId(this.logBook.getId());
        setDeviceId((int) this.logBook.getDevice().getId());
        setMasterSerialNumber(this.logBook.getDevice().getSerialNumber());
        Optional<Instant> lastLogBook = this.logBook.getLatestEventAdditionDate();
        setOfflineLogBookSpec(new OfflineLogBookSpecImpl(logBook.getLogBookSpec()));
        setLastLogBook(lastLogBook.isPresent() ? Date.from(lastLogBook.get()) : null);
    }

    @Override
    public long getLogBookId() {
        return logBookId;
    }

    public void setLogBookId(long logBookSpecId) {
        this.logBookId = logBookSpecId;
    }

    @Override
    @XmlElement(type = OfflineLogBookSpecImpl.class)
    public OfflineLogBookSpec getOfflineLogBookSpec() {
        return offlineLogBookSpec;
    }

    public void setOfflineLogBookSpec(OfflineLogBookSpec offlineLogBookSpec) {
        this.offlineLogBookSpec = offlineLogBookSpec;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
    }

    public void setMasterSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public Date getLastReading() {
        return lastReading;
    }

    void setLastLogBook(Date lastLogBook) {
        this.lastReading = lastLogBook;
    }

    @Override
    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    public DeviceIdentifier getDeviceIdentifier() {
        if (identificationService != null)
            deviceIdentifier = identificationService.createDeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
        return deviceIdentifier;
    }

    @Override
    @XmlElements( {
            @XmlElement(type = LogBookIdentifierById.class),
            @XmlElement(type = LogBookIdentifierByObisCodeAndDevice.class),
            @XmlElement(type = LogBookIdentifierByDeviceAndObisCode.class),
            @XmlElement(type = LogBookIdentifierForAlreadyKnowLogBook.class),
    })
    public LogBookIdentifier getLogBookIdentifier() {
        if (logBookIdentifier == null && this.identificationService != null)
            logBookIdentifier = this.identificationService.createLogbookIdentifierForAlreadyKnownLogbook(logBook, getDeviceIdentifier());
        return logBookIdentifier;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
