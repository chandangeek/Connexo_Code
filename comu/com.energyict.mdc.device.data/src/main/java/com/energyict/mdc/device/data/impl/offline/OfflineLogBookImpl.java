package com.energyict.mdc.device.data.impl.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;

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
    private int logBookTypeId;

    public OfflineLogBookImpl(LogBook logBook) {
        this.logBook = logBook;
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
        setLogBookTypeId((int) this.logBook.getLogBookType().getId());
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
    public int getLogBookTypeId() {
        return logBookTypeId;
    }

    @Override
    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setLogBookId(long logBookId) {
        this.logBookId = logBookId;
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

    public void setLogBookTypeId(int logBookTypeId) {
        this.logBookTypeId = logBookTypeId;
    }
}
