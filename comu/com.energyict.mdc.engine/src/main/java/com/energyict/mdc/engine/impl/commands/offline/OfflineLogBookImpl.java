package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.Date;

/**
 * Represents an Offline version of a {@link com.energyict.mdc.protocol.api.device.BaseLogBook}
 *
 * @author sva
 * @since 07/12/12 - 14:36
 */
public class OfflineLogBookImpl implements OfflineLogBook {

    /**
     * The {@link com.energyict.mdc.protocol.api.device.BaseLogBook} which is going offline
     */
    private final LogBook logBook;
    private IdentificationService identificationService;

    private final Device device;

    /**
     * The database ID of this {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice devices'} {@link com.energyict.mdc.protocol.api.device.BaseLogBook}
     */
    private long logBookId;

    /**
     * The id of the {@link com.energyict.mdc.protocol.api.device.BaseDevice} which owns this LogBook.
     */
    private int deviceId;

    /**
     * The serialNumber of the {@link com.energyict.mdc.protocol.api.device.BaseDevice Device}
     */
    private String serialNumber;
    /**
     * The Date from where to start fetching data from the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}
     */
    private Date lastLogBook;
    /**
     * The ObisCode of the LogBookSpec
     */
    private ObisCode obisCode;
    /**
     * The ID of the LogBookType
     */
    private long logBookTypeId;

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
        setLogBookTypeId(this.logBook.getLogBookType().getId());
        setObisCode(this.logBook.getLogBookSpec().getDeviceObisCode());
        setMasterSerialNumber(this.logBook.getDevice().getSerialNumber());
        setLastLogBook(this.logBook.getLastLogBook());
    }

    @Override
    public long getLogBookId() {
        return logBookId;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    @Override
    public String getMasterSerialNumber() {
        return serialNumber;
    }

    @Override
    public Date getLastLogBook() {
        return lastLogBook;
    }

    @Override
    public long getLogBookTypeId() {
        return logBookTypeId;
    }

    @Override
    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        return this.identificationService.createLogbookIdentifierForAlreadyKnownLogbook(logBook);
    }

    public void setLogBookId(long logBookSpecId) {
        this.logBookId = logBookSpecId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setMasterSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setLastLogBook(Date lastLogBook) {
        this.lastLogBook = lastLogBook;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public void setLogBookTypeId(long logBookTypeId) {
        this.logBookTypeId = logBookTypeId;
    }
}
