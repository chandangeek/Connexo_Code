/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/errortest/ErrorTestOutputStream.java $
 * Version:     
 * $Id: ErrorTestOutputStream.java 3807 2011-12-01 14:25:52Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.07.2011 14:02:16
 */
package com.elster.protocols.errortest;

import com.elster.coding.CodingUtils;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * This output stream manipulates bits of an output stream for testing purposes.
 * <P>
 * It can be used to simulate a bad modem connection.
 *
 * @author osse
 */
public class ErrorTestOutputStream extends FilterOutputStream
{
  private volatile int nextErrorByte = 0; //TODO: switch to Atomic version or synchronize access.
  private final int errorRate;
  private final Random rand = new Random();

  private void calcNextErrorByte()
  {
    if (errorRate > 0)
    {
      nextErrorByte = (int)Math.abs(rand.nextGaussian() * errorRate);
    }
    else
    {
      nextErrorByte = -1;
    }
  }

  private int swapRandBit(final int b)
  {
    final int bitNo = rand.nextInt(8);
    return b ^ (0x01 << bitNo);
  }

  public ErrorTestOutputStream(final OutputStream out, final int errorRate)
  {
    super(out);
    this.errorRate = errorRate;
    rand.setSeed(416674);
    calcNextErrorByte();
  }

  @Override
  public void write(int b) throws IOException
  {
    if (nextErrorByte == 0)
    {
      calcNextErrorByte();
      b = swapRandBit(b);
    }
    else
    {
      if (nextErrorByte >= 0)
      {
        nextErrorByte--;
      }
    }
    out.write(b);
  }

  
  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    if (nextErrorByte < len && nextErrorByte >= 0)
    {
      b = CodingUtils.copyOfRange(b, off, off + len);
      b[nextErrorByte] = (byte)(swapRandBit(0xFF & b[nextErrorByte]));
      calcNextErrorByte();
      out.write(b, 0, len);
    }
    else
    {
      if (nextErrorByte >= 0)
      {
        nextErrorByte -= len;
      }
      out.write(b, off, len);
    }
  }

  @Override
  public void write(final byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }

}
