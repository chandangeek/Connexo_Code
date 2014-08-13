package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 16:02
 */
public class ComStatisticsImpl implements ComStatistics, HasId {

    private long id;
    private long nrOfBytesSent;
    private long nrOfBytesReceived;
    private long nrOfPacketsSent;
    private long nrOfPacketsReceived;
    private Date modDate;


//    private void validate(ComStatisticsShadow shadow) throws BusinessException {
//        validateNotNegative(shadow.getNrOfBytesSent(), "comstatistics.nrofbytessent");
//        validateNotNegative(shadow.getNrOfBytesRead(), "comstatistics.nrofbytesread");
//        validateNotNegative(shadow.getNrOfPacketsSent(), "comstatistics.nrofpacketssent");
//        validateNotNegative(shadow.getNrOfPacketsRead(), "comstatistics.nrofbpacketssent");
//    }

//    private void validateNotNegative (long propertyValue, String propertyName) throws InvalidValueException {
//        if (propertyValue < 0) {
//            throw new InvalidValueException("XcannotBeNegative", "\"{0}\" should be a positive number", propertyName);
//        }
//    }

    /**
     * Gets the number of bytes sent during the ComSession
     *
     * @return The number of bytes sent
     */
    @Override
    public long getNrOfBytesSent() {
        return nrOfBytesSent;
    }

    /**
     * Gets the number of bytes received during the ComSession
     *
     * @return The number of bytes received
     */
    @Override
    public long getNrOfBytesReceived() {
        return nrOfBytesReceived;
    }

    /**
     * Gets the number of packets sent during the ComSession
     *
     * @return The number of packets sent
     */
    @Override
    public long getNrOfPacketsSent() {
        return nrOfPacketsSent;
    }

    /**
     * Gets the number of packets received during the ComSession
     *
     * @return The number of packets received
     */
    @Override
    public long getNrOfPacketsReceived() {
        return nrOfPacketsReceived;
    }

    @Override
    public long getId() {
        return id;
    }

     void setNumberOfBytesReceived(long value) {
        nrOfBytesReceived = value;
    }

    void setNrOfBytesSent(long nrOfBytesSent) {
        this.nrOfBytesSent = nrOfBytesSent;
    }

    void setNrOfPacketsSent(long nrOfPacketsSent) {
        this.nrOfPacketsSent = nrOfPacketsSent;
    }

    void setNrOfPacketsReceived(long nrOfPacketsReceived) {
        this.nrOfPacketsReceived = nrOfPacketsReceived;
    }
}