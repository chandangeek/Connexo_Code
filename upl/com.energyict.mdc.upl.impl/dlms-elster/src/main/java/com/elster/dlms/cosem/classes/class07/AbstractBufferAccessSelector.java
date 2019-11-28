/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/AbstractBufferAccessSelector.java $
 * Version:     
 * $Id: AbstractBufferAccessSelector.java 2664 2011-02-11 12:37:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 11, 2011 11:41:39 AM
 */

package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.cosem.objectmodel.CosemAccessSelector;
import java.util.List;

/**
 * Abstract base class for access selectors for the buffer attribute of COSEM class id 7.
 * 
 * @author osse
 */
public abstract class AbstractBufferAccessSelector implements CosemAccessSelector
{
  /**
   * Filters the columns as they expected by applying this access selector.
   *
   * @param all All columns.
   * @return The filtered columns.
   */
  public abstract List<CaptureObjectDefinition> filterActiveObjectDefinitions(List<CaptureObjectDefinition> all);
}
