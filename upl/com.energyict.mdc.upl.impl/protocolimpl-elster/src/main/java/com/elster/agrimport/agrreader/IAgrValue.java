/* File:
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/IAgrValue.java $
 * Version:
 * $Id: IAgrValue.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.07.2010 16:31:25
 */
package com.elster.agrimport.agrreader;

/**
 * This interface is the basis for archive values in an AGR file.
 *
 * @author osse
 */
public interface IAgrValue<T>
{
  //not used by now:
  //enum ValueType {INTEGER, LONG, BIGDECIMAL, STRING, DATE, STATED_BIGDECIMAL}
  /**
   * Returns the archive value.
   *
   * @return The value.
   */
  T getValue();

  /**
   * Set the archive value.
   *
   * @param value the value to set
   */
  void setValue(final T value);

}
