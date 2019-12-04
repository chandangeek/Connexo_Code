/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/errortest/ErrorTestProtocol.java $
 * Version:     
 * $Id: ErrorTestProtocol.java 3807 2011-12-01 14:25:52Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.05.2010 16:04:25
 */
package com.elster.protocols.errortest;

import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IStreamProtocol;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class uses the {@link ErrorTestInputStream} and {@link ErrorTestOutputStream} for testing purposes.
 *
 * @author osse
 */
public class ErrorTestProtocol extends FilterProtocol
{
  private final InputStream errorInputStream;
  private final OutputStream errorOutputStream;

  public ErrorTestProtocol(final IStreamProtocol sublayer, final int errorRateRead, final int errorRateWrite,
                           final int delayMs)
  {
    super(sublayer);
    errorInputStream = new ErrorTestInputStream(sublayer.getInputStream(), errorRateRead);

    OutputStream localOut = new ErrorTestOutputStream(sublayer.getOutputStream(), errorRateWrite);
    if (delayMs > 0)
    {
      localOut = new ErrorTestDelayOutputStream(localOut, delayMs);
    }

    errorOutputStream = localOut;

  }

  @Override
  public InputStream getInputStream()
  {
    return errorInputStream;
  }

  @Override
  public OutputStream getOutputStream()
  {
    return errorOutputStream;
  }

}
