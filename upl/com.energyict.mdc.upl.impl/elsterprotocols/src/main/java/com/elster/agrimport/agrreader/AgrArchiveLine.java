/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrArchiveLine.java $
 * Version:     
 * $Id: AgrArchiveLine.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 11:09:18
 */
package com.elster.agrimport.agrreader;

import java.util.ArrayList;

/**
 * This class is an ArrayList containing agr value.<P>
 * Each instance represents one line from an agr file.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrArchiveLine extends ArrayList<IAgrValue>
{
  public AgrArchiveLine()
  {
    super();
  }

  public AgrArchiveLine(final int initialCapacity)
  {
    super(initialCapacity);
  }

}
