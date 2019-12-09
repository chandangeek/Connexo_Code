/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/dataformat/ProtocolDataFormat.java $
 * Version:     
 * $Id: ProtocolDataFormat.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 09:33:58
 */

package com.elster.protocols.dataformat;

import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IStreamProtocol;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class adds and checks 7E1 parity bits to the streams.<P>
 * It uses the {@link DataFormatInputStream} and {@link DataFormatOutputStream} to achieve this.
 *
 * @author osse
 */
public class ProtocolDataFormat extends FilterProtocol
{
  private final InputStream inputStream;
  private final OutputStream outputStream;

  public ProtocolDataFormat(IStreamProtocol sublayer)
  {
    super(sublayer);
    inputStream=  new DataFormatInputStream(sublayer.getInputStream());
    outputStream = new DataFormatOutputStream(sublayer.getOutputStream());
  }


  @Override
  public InputStream getInputStream()
  {
    return inputStream;
  }

  @Override
  public OutputStream getOutputStream()
  {
    return outputStream;
  }

}
