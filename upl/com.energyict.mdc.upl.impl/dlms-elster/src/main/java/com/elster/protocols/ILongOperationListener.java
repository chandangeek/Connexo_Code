/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/ILongOperationListener.java $
 * Version:
 * $Id: ILongOperationListener.java 3843 2011-12-12 16:55:48Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.12.2011 14:12:32
 */
package com.elster.protocols;

/**
 * This interface ...
 *
 * @author osse
 */
public interface ILongOperationListener
{
  enum Operation
  {
    READ,
    WRITE
  }

  void longOperationStart(Object sender, Operation operation);

  void longOperationBytesTransfered(Object sender, Operation operation, long byteCount);

  void longOperationEnd(Object sender, Operation operation);

}
