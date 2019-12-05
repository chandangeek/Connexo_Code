/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/IDlmsDataProvider.java $
 * Version:
 * $Id: IDlmsDataProvider.java 3188 2011-07-12 15:17:01Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.07.2011 10:04:21
 */
package com.elster.dlms.types.data;

/**
 * Interface for classes which provides a DlmsData presentation of its values.
 *
 * @author osse
 */
public interface IDlmsDataProvider
{
  DlmsData toDlmsData();
}
