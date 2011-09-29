package com.elster.utils.lis200.profile.agr;

import com.elster.utils.lis200.agrmodel.ArchiveData;
import com.elster.utils.lis200.agrmodel.ArchiveLine;
import com.elster.utils.lis200.profile.IArchiveLineData;
import com.elster.utils.lis200.profile.IArchiveRawData;

/**
 * Interface class to allow ArchiveData to be processed by class ProcessArchiveData
 *
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 11:32:53
 */
public class AgrArchiveData
        implements IArchiveRawData {

    /* class with all read data...*/
    private ArchiveData archive;

    /* information about archive line */
    private AgrArchiveLineInfo lineInfo;

    private int linePointer = 0;

    /**
     * Constructor of interface class for archive processing
     *
     * @param archive - read archive data
     */
    public AgrArchiveData(ArchiveData archive) {
        this.archive = archive;

        switch (archive.getFileType()) {
            case UNKNOWN:
                lineInfo = new AgrArchiveLineInfo(this.archive.getColumns());
                break;
            case DSFG:
                lineInfo = new DsfgAgrArchiveLineInfo(this.archive.getColumns());
                break;
            case LIS200:
                lineInfo = new Lis200AgrArchiveLineInfo(this.archive.getColumns());
                break;
        }
    }

    /*--------------------------------------------------------------------------------------------
     *
     * IArchiveRawData interface methods
     *
     *------------------------------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void resetRecordPointer() {
        linePointer = 0;
     }

    /**
     * {@inheritDoc}
     */
    public IArchiveLineData getNextRecord() {
        if (linePointer >= archive.getLineCount()) {
            return null;
        }

        ArchiveLine al = archive.getLine(linePointer);
        linePointer++;

        return new AgrArchiveLine(lineInfo, al);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfChannels() {
       return lineInfo.getNumberOfValues();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSystemStatus() {
        return this.lineInfo.getSystemStateCol() >= 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasInstanceStatus() {
        return this.lineInfo.getNumberOfInstanceStateCols() >= 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasValueStatus() {
        return this.lineInfo.isHavingValueStateCols();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasEvent() {
        return this.lineInfo.getEventCol() >= 0;
    }

    /**
     * Gets the absolute column no of value column <index>
     *
     * @return column no
     */
    public int getValueColumn(int index ) {
        return this.lineInfo.getValueColumn(index);
    }
}
