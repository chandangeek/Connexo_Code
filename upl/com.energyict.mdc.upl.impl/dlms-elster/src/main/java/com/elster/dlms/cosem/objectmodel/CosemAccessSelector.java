/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemAccessSelector.java $
 * Version:
 * $Id: CosemAccessSelector.java 2858 2011-04-20 15:18:49Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 3, 2011 1:07:32 PM
 */

package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.types.data.DlmsData;

/**
 * Model interface for the COSEM access selector.
 *
 * @author osse
 */
public interface CosemAccessSelector
{
  int getId();
  DlmsData toDlmsData();
}
