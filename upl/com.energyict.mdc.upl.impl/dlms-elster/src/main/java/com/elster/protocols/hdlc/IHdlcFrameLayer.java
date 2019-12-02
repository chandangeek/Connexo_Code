/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/IHdlcFrameLayer.java $
 * Version:
 * $Id: IHdlcFrameLayer.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.07.2011 12:20:56
 */
package com.elster.protocols.hdlc;

import java.io.IOException;

/**
 * This interface ...
 *
 * @author osse
 */
public interface IHdlcFrameLayer
{
  void sendFrame(HdlcFrame frame) throws IOException;
  HdlcFrame receiveFrame(int timeout, int interOctetTimeout) throws IOException;
}
