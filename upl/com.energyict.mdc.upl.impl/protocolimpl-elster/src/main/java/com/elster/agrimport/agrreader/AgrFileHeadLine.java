/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrFileHeadLine.java $
 * Version:     
 * $Id: AgrFileHeadLine.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 11:47:41
 */
package com.elster.agrimport.agrreader;

/**
 * This class represents the first line of an AGR file.
 * <p/>
 * The line <br>
 * {@code "M	Archiv 3 (Meas. period archive EK260 V2.x, EK230, EK220)	4001210	EK260"} <br>
 * will be interpreted as follows: <br>
 * <br>
 * <table>
 * <tr><td>type = </td><td>"M"</td></tr>
 * <tr><td>archiveName = </td><td>"Archiv 3 (Meas. period archive EK260 V2.x, EK230, EK220)"</td></tr>
 * <tr><td>serialNumber = </td><td>"4001210"</td></tr>
 * <tr><td>deviceType = </td><td>"EK260"</td></tr>
 * </table>
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrFileHeadLine
{
  //M	Archiv 3 (Meas. period archive EK260 V2.x, EK230, EK220)	4001210	EK260
  private String type;
  private String archiveName;
  private String serialNumber;
  private String deviceType;

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(final String type)
  {
    this.type = type;
  }

  /**
   * @return the archiveName
   */
  public String getArchiveName()
  {
    return archiveName;
  }

  /**
   * @param archiveName the archiveName to set
   */
  public void setArchiveName(final String archiveName)
  {
    this.archiveName = archiveName;
  }

  /**
   * @return the serialNumber
   */
  public String getSerialNumber()
  {
    return serialNumber;
  }

  /**
   * @param serialNumber the serialNumber to set
   */
  public void setSerialNumber(final String serialNumber)
  {
    this.serialNumber = serialNumber;
  }

  /**
   * @return the deviceType
   */
  public String getDeviceType()
  {
    return deviceType;
  }

  /**
   * @param deviceType the deviceType to set
   */
  public void setDeviceType(final String deviceType)
  {
    this.deviceType = deviceType;
  }

  public void parseLine(final String line) throws AgrFileException
  {
    String[] fields = line.split("\t", -1);
    if (fields.length != 4)
    {
      throw new AgrFileException("Wrong entry count in agr-HeadLine");
    }

    //M	Archiv 3 (Meas. period archive EK260 V2.x, EK230, EK220)	4001210	EK260

    setType(fields[0]);
    setArchiveName(fields[1]);
    setSerialNumber(fields[2]);
    setDeviceType(fields[3]);
  }

}
