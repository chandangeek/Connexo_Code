/* File:
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/IAgrColTypeDefinitions.java $
 * Version:
 * $Id: IAgrColTypeDefinitions.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 10:24:23
 */
package com.elster.agrimport.agrreader;

/**
 * This interface embeds the AgrColType enumeration
 *
 * @author osse
 */
public interface IAgrColTypeDefinitions
{
  public enum AgrColType
  {
    ORDERNUMBER,
    GLOBORDERNUMBER,
    TIMESTAMP,
    NUMBER,
    STATED_NUMBER,
    COUNTER,
    STATED_COUNTER,
    INTERVALL,
    STATED_INTERVALL,
    APHANUMERIC,
    STATUS_INT,
    STATUS_STRING,
    STATUS_REGISTER,
    HEX,
    DATA,
    UNKNOWN
  }

}
