/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/ICosemApplicationLayerTask.java $
 * Version:
 * $Id: ICosemApplicationLayerTask.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.08.2011 17:17:24
 */
package com.elster.dlms.cosem.applicationlayer;

import java.io.IOException;

/**
 * This interface ...
 *
 * @author osse
 */
public interface ICosemApplicationLayerTask
{
  public void execute(CosemApplicationLayer applicationLayer) throws IOException;
}
