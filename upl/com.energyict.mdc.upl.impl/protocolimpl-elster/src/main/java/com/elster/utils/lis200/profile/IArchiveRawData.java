package com.elster.utils.lis200.profile;

/**
 * Interface to access raw data
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:42:07
 */
public interface IArchiveRawData {

    /* TODO: add additional information like type of value for channel.... */

    /**
     * resets the record pointer, so getNextRecord() delivers the first record
     */
    public void resetRecordPointer();
    
    /**
     * gets the next record to process.
     *
     * @return IArchiveLineData, null if there is no further data
     */
    public IArchiveLineData getNextRecord();

    /**
     * gets the number of storable values in an archive line (for channels)
     *
     * @return number of values
     */
    public int getNumberOfChannels();

    /**
     * Shows if there is a system status in the archive
     *
     * @return true if there is a "system state" column, otherwise false
     */
    public boolean hasSystemStatus();

    /**
     * shoes if line contains one or more "instance status columns" (lis200 sepecific)
     *
     * @return true if there are "instance state columns"
     */
    public boolean hasInstanceStatus();

    /**
     * Shows if there are separate status columns for values
     *
     * @return true if there are separate "value state" columns, otherwise false
     */
    public boolean hasValueStatus();

    /**
     * Shows if there is a event column in the archive
     *
     * @return true if there is a event column, otherwise false
     */
    public boolean hasEvent();
}
