package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.AgrColumnHeader;
import com.elster.agrimport.agrreader.AgrFileType;

import java.util.*;

/**
 * This class hold data of a complete archive
 * 
 * User: heuckeg
 * Date: 29.06.2010
 * Time: 09:14:14
 */
public class ArchiveData {

    /* name of archive */
    private String archiveName = "";
    /* device id */
    private String archiveDevice = "";
    /* device type */
    private String archiveDevType = "";
    /* type of archive */
    private AgrFileType fileType = AgrFileType.UNKNOWN;
    /* additional info of agr head */
    private Map<String, String> info = new HashMap<String, String>();

    private List<AgrColumnHeader> columns = new ArrayList<AgrColumnHeader>();

    private List<ArchiveLine> lines = new ArrayList<ArchiveLine>();


    public ArchiveData() {

    }

    /*************************************************************************************
     *
     * Getter and Setter
     *
     ************************************************************************************/

    /**
     * Getter for archive name
     *
     * @return name of archive
     */
    public String getArchiveName() {
        return archiveName;
    }

    /**
     * Setter for archive name
     *
     * @param archiveName - archive name to set
     */
    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    /**
     * Getter for device id
     *
     * @return device id
     */
    public String getArchiveDevice() {
        return archiveDevice;
    }

    /**
     * Setter for device id
     *
     * @param archiveDevice - device id
     */
    public void setArchiveDevice(String archiveDevice) {
        this.archiveDevice = archiveDevice;
    }

    /**
     * Getter for device type
     *
     * @return device type
     */
    public String getArchiveDeviceType() {
        return archiveDevType;
    }

    /**
     * Setter for device type
     *
     * @param archiveDevType - device type
     */
    public void setArchiveDeviceType(String archiveDevType) {
        this.archiveDevType = archiveDevType;
    }

    /**
     * Set type of file
     *
     * @param type - of agr file
     */
    public void setFileType(AgrFileType type) {
        fileType = type;
    }

    /**
     * Get type of file
     *
     * @return file type
     */
    public AgrFileType getFileType() {
        return fileType;
    }

    /**
     * Getter for additional head info
     *
     * @param name - of info value
     * @return map with info
     */
    public String getInfo(String name) {
        return info.get(name);
    }

    /**
     *  Setter for additional info (as map)
     *
     * @param info - map with info
     */
    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    /**
     * Setter for column information
     *
     * @param columns - list of AgrColumnHeader
     */
    public void setColumns(List<AgrColumnHeader> columns) {
        this.columns = columns;
    }

    /**
     * Gets number of columns
     *
     * @return number of cols
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Getter for one column
     *
     * @param index - of column
     *
     * @return wanted column
     */
    public AgrColumnHeader getColumn(int index) {
        return this.columns.get(index);
    }

    /**
     * Gets all columns
     *
     * @return columns
     */
    public List<AgrColumnHeader> getColumns() {
        return this.columns;
    }

    /**
     * Method to add an archive lines
     *
     * @param line - new archive lines
     */
    public void add(ArchiveLine line) {
        this.lines.add(line);
    }

    /**
     * Gets the amount of archive lines
     *
     * @return lineCount
     */
    public int getLineCount() {
        return this.lines.size();
    }

    /**
     * Gets all archive lines
     *
     * @return all archive lines
     */
    public List<ArchiveLine> getLines() {
        return this.lines;
    }

    /**
     * Gets a single archive line
     *
     * @param index - of archive line
     * @return the archive line
     */
    public ArchiveLine getLine(int index) {
        return this.lines.get(index);
    }

    public String toString() {
        StringBuilder text = new StringBuilder();

        text.append(String.format("Device : %s\r\n", this.archiveDevice));
        text.append(String.format("Archive: %s\r\n", this.archiveName));
        text.append(String.format("Type   : %s\r\n", this.archiveDevType));

        /* Head lines */
        String line = "";
        for (AgrColumnHeader col: this.columns) {
            if (line.length() > 0)
                line = line + ";";
            line = line + col.getHeadName();
        }
        text.append(line);
        text.append("\r\n");

        line = "";
        for (AgrColumnHeader col: this.columns) {
            if (line.length() > 0)
                line = line + ";";
            line = line + col.getHeadUnit();
        }
        text.append(line);
        text.append("\r\n");

        line = "";
        for (AgrColumnHeader col: this.columns) {
            if (line.length() > 0)
                line = line + ";";
            line = line + col.getHeadColumnType();
        }
        text.append(line);
        text.append("\r\n");

        line = "";
        for (AgrColumnHeader col: this.columns) {
            if (line.length() > 0)
                line = line + ";";
            line = line + col.getColumnType();
        }
        text.append(line);
        text.append("\r\n");

        /* data */
        for (ArchiveLine al: this.lines) {
            text.append(al.toString());
        }

        return text.toString();
    }
}
