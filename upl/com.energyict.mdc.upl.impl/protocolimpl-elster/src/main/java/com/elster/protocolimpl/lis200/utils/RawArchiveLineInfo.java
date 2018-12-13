package com.elster.protocolimpl.lis200.utils;

import com.elster.protocolimpl.lis200.ChannelDefinition;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 12:23:48
 */
public class RawArchiveLineInfo {

    /* no of values in an archive line */
    protected int noOfValues = 0;
    /* column of global ono */
    protected int gonoCol = -1;
    /* column of archive ono */
    protected int onoCol = -1;
    /* column of time stamp */
    protected int tstCol = -1;
    /* column of event column */
    protected int eventCol = -1;
    /* column of system state column */
    protected int systemStateCol = -1;
    /* list of instance state columns */
    protected List<Integer> instanceStateCols = new ArrayList<Integer>();

    protected int topIndex = 0;
    /* list of data columns */
    protected HashMap<Integer, ChannelDefinition> valueIndex = new HashMap<Integer, ChannelDefinition>();
    /* format of time stamp */
    DateFormat rdf = null;

    public RawArchiveLineInfo(String archiveStructure) {
        this(archiveStructure, "yyyy-MM-dd,HH:mm:ss", "GMT0");
    }

    /**
     * Constructor for a archive line of unknown type
     *
     * @param archiveStructure - of archive
     * @param dateFormat - format definition for data
     * @param timeZone - time zone for date conversion
     */
    public RawArchiveLineInfo(String archiveStructure, String dateFormat, String timeZone) {

        rdf = new SimpleDateFormat(dateFormat);
        rdf.setTimeZone(TimeZone.getTimeZone(timeZone));

        String[] values = archiveStructure.toUpperCase().split(",");

        noOfValues = values.length;

        int col = -1;
        for (String name : values) {
            col++;
            if ((gonoCol < 0) && (ArchiveColumn.isGONO(name))) {
                gonoCol = col;
                continue;
            }
            if ((onoCol < 0) && (ArchiveColumn.isONO(name))) {
                onoCol = col;
                continue;
            }
            if ((tstCol < 0) && (ArchiveColumn.isTST(name))) {
                tstCol = col;
                continue;
            }
            if (ArchiveColumn.isSystemState(name)) {
                systemStateCol = col;
                continue;
            }
            if (ArchiveColumn.isInstState(name)) {
                instanceStateCols.add(col);
                continue;
            }
            if (ArchiveColumn.isEvent(name)) {
                eventCol = col;
                continue;
            }
            if (name.equalsIgnoreCase("CHECK")) {
                continue;
            }
            if (name.startsWith("CHN"))
            {
                ChannelDefinition cd = new ChannelDefinition(name, col);
                addValueColumn(cd);
            }
        }
    }

    /**
     * internal method to add a value column to the internal store
     *
     * @param cd - channel definition data
     */
    private void addValueColumn(ChannelDefinition cd) {
        int index = cd.getChannelNo();
        if ( index >= topIndex)
        {
            topIndex = index + 1;
        }
        valueIndex.put(index, cd);
    }

    private ChannelDefinition getChannelDefinition(int index) {
        if (valueIndex.containsKey(index)) {
            return valueIndex.get(index);
        }
        return null;
    }

    /**
     * get number of values in an archive line
     *
     * @return number of values
     */
    public int getNumberOfValuesPerLine() {
        return noOfValues;
    }

    public DateFormat getDateFormat() {
        return rdf;
    }

    @SuppressWarnings({"unused"})
    public int getGonoCol() {
        return gonoCol;
    }

    @SuppressWarnings({"unused"})
    public int getOnoCol() {
        return onoCol;
    }

    public int getTstCol() {
        return tstCol;
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
        return instanceStateCols.size();
    }

    /**
     * @return the number of columns with data
     */
    public int getNumberOfChannels() {
        return topIndex;
    }

    /**
     * @param index - sequence number of value
     * @return index of value column
     */
    public int getValueColumn(int index) {
        ChannelDefinition cd = getChannelDefinition(index);
        return (cd != null) ? cd.getArchiveColumn() : 0;
    }

    /**
     * @param index - sequence number of value
     * @return true if raw value is index
     */
    public boolean isValueCounter(int index) {
        ChannelDefinition cd = getChannelDefinition(index);
        return (cd != null) && (cd.getChannelType().equals("C"));
    }

    public int getValueOverFlow(int index) {
        ChannelDefinition cd = getChannelDefinition(index);
        return (cd != null) ? cd.getChannelOv() : 0;
    }

    /**
     * Gets the index of an instance state column
     *
     * @param index - of the instance state column
     * @return col no for value addressed by index
     */
    @SuppressWarnings({"unused"})
    public int getInstanceStateCol(int index) {
        return instanceStateCols.get(index);
    }

    /**
     * Gets the complete list of instanceStateCols
     *
     * @return List<Integer> - all instance state cols
     */
    public List<Integer> getInstanceStateCols() {
        return instanceStateCols;
    }

    /**
     * Translates a system state to an ei server interval state
     *
     * @param stateList - device dependent...
     * @return ei server interval state
     */
    @SuppressWarnings({"unused"})
    public int translateSystemStateToEIState(int stateList) {
        return 0;
    }

    /**
     * Translates an instance state to an ei server interval state
     *
     * @param state - device dependent...
     * @return ei server interval state
     */
    @SuppressWarnings({"unused"})
    public int translateInstanceStateToEIState(int state) {
        return 0;
    }

    @SuppressWarnings({"unused"})
    public int translateValueStateToEIState(int state) {
        return 0;
    }

}
