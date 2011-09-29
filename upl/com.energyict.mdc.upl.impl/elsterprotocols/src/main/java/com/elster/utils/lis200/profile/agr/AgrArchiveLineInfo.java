package com.elster.utils.lis200.profile.agr;

import com.elster.agrimport.agrreader.AgrColumnHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 12:23:48
 */
public class AgrArchiveLineInfo {

    /**
     * archive has a event column
     */
    protected int eventCol = -1;
    /**
     * archive has a system state column
     */
    protected int systemStateCol = -1;
    /**
     * if there are instance state columns in the archive...
     */
    protected int instanceStateCols = 0;
    /**
     * archive has "stated" values
     */
    protected boolean hasValueStateCols = false;
    /**
     * indexes to columns with data
     */
    protected List<Integer> valueIndex = new ArrayList<Integer>();
    /**
     * marker, if there are only stated values in the archive
     */
    private boolean hasOnlyStatedValues = false;

    /**
     * Constructor for a archive line of unknown type
     *
     * @param headerInfo - of archive
     */
    public AgrArchiveLineInfo(List<AgrColumnHeader> headerInfo) {
        int cStatedValues = 0;
        for (int i = 0; i < headerInfo.size(); i++) {
            AgrColumnHeader col = headerInfo.get(i);
            switch (col.getColumnType()) {
                case STATED_COUNTER:
                case STATED_INTERVALL:
                case STATED_NUMBER:
                    cStatedValues++;
                case COUNTER:
                case INTERVALL:
                case NUMBER:
                    valueIndex.add(i);
            }
        }
        hasOnlyStatedValues = valueIndex.size() == cStatedValues;
    }

    /**
     * @return true if there are only stated values in the archive
     */
    protected boolean isHavingOnlyStatedValues() {
        return hasOnlyStatedValues;
    }

    /**
     * @return the event column index, -1 if column doesn't exist
     */
    public int getEventCol() {
        return eventCol;
    }

    /**
     * @return the system state column index, -1 if column doesn't exist
     */
    public int getSystemStateCol() {
        return systemStateCol;
    }

    /**
     * @return number of instance state columns
     */
    public int getNumberOfInstanceStateCols() {
        return instanceStateCols;
    }

    /**
     * @return true if the archive have state columns for each value
     */
    public boolean isHavingValueStateCols() {
        return hasValueStateCols;
    }

    /**
     * @return the number of columns with data
     */
    public int getNumberOfValues() {
        return valueIndex.size();
    }

    /**
     * @param index - sequence number of value
     * @return index of value column
     */
    public int getValueColumn(int index) {
        return valueIndex.get(index);
    }

    /**
     * Gets the index of an instance state column
     *
     * @param index - of the instance state column
     * @return
     */
    public int getInstanceStateCol(int index) {
        return -1;
    }

    /**
     * Translates a system state to an ei server interval state
     *
     * @param stateList - device dependent...
     * @return ei server interval state
     */
    public int translateSystemStateToEIState(int stateList) {
        return 0;
    }

    /**
     * Translates an instance state to an ei server interval state
     *
     * @param state - device dependent...
     * @return ei server interval state
     */
    public int translateInstanceStateToEIState(int state) {
        return 0;
    }

    public int translateValueStateToEIState(int state) {
        return 0;
    }

}
