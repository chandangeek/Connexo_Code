/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/errortest/ErrorTestInputStream.java $
 * Version:     
 * $Id: ErrorTestInputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.05.2010 15:35:32
 */
package com.elster.protocols.errortest;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * This input stream manipulates bits of an input stream for testing purposes.
 * <P>
 * It can be used to simulate a bad modem connection.
 *
 * @author osse
 */
public class ErrorTestInputStream extends FilterInputStream
{
  private int nextErrorByte = 200;
  private int errorRate = 0;
  private final Random rand = new Random();

  private void calcNextErrorByte()
  {
    if (errorRate > 0)
    {
      nextErrorByte =  (int)Math.abs(rand.nextGaussian() *errorRate);
    }
    else
    {
      nextErrorByte = -1;
    }
  }

  public ErrorTestInputStream(InputStream in, int errorRate)
  {
    super(in);
    this.errorRate= errorRate;
    rand.setSeed(641674);
    calcNextErrorByte();
  }

  public void setErrorRate(int errorRate)
  {
    this.errorRate = errorRate;
    calcNextErrorByte();
  }

  private int swapRandBit(int b)
  {
    int bitNo = rand.nextInt(8);
    return b ^ (0x01 << bitNo);
  }

  @Override
  public int read() throws IOException
  {
    if (nextErrorByte == 0)
    {
      int b = in.read();

      if (b >= 0)
      {
        calcNextErrorByte();
        return swapRandBit(b);
      }
      else
      {
        return b;
      }
    }
    else
    {
      nextErrorByte--;
      return in.read();
    }

  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int bytesRead = in.read(b, off, len);

    if (nextErrorByte < bytesRead && nextErrorByte >= 0)
    {
      b[nextErrorByte + off] = (byte)(swapRandBit(0xFF & b[nextErrorByte + off]));
      calcNextErrorByte();
    }
    else
    {
      nextErrorByte -= bytesRead;
    }

    return bytesRead;
  }

}
