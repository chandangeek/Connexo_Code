/* File:
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/IStatedAgrValue.java $
 * Version:
 * $Id: IStatedAgrValue.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.07.2010 14:09:23
 */
package com.elster.agrimport.agrreader;

/**
 * This interface must be implemented by ArchiveValue which have an status.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public interface IStatedAgrValue
{
  /**
   * Returns the status of the archive value
   *
   * @return The status
   */
  public int getStatus();

  /**
   * Sets the status of the archive value.
   *
   * @param status - state to set
   */
  public void setStatus(int status);

}
