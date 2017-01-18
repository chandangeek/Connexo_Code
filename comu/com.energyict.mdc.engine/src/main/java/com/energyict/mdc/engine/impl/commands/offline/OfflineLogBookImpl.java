package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineLogBookSpec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    private final LogBook logBook;
    private final Device device;
    private IdentificationService identificationService;

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
    private Date lastLogBook;

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
        Optional<Instant> lastLogBook = this.logBook.getLastLogBook();
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
    @XmlAttribute
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
        return lastLogBook;
    }

    void setLastLogBook(Date lastLogBook) {
        this.lastLogBook = lastLogBook;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        return this.identificationService.createLogbookIdentifierForAlreadyKnownLogbook(logBook);
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
