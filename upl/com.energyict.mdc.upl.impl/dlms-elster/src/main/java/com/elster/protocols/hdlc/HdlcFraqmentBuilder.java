/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcFraqmentBuilder.java $
 * Version:     
 * $Id: HdlcFraqmentBuilder.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.05.2010 11:57:01
 */
package com.elster.protocols.hdlc;

import com.elster.coding.CodingUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This class builds HDLC I frames from an input stream for an HDLC channel.
 * <P>
 * It automatically splits the data from the input stream into several
 * I frames (with correct fragmentation bits) as necessary.
 * <P>
 * The (maximum) length of the information field is determined by the according property of
 * the channel.
 * <P>
 * The last I frame will be produced if the input stream signals the end of file by returning -1
 * in the read methods.
 *
 * @author osse
 */
public class HdlcFraqmentBuilder
{
  private static final int NEXT_BYTE_NOT_READ = 500;
  private final HdlcInformationBlockOut block;
  private final InputStream in;
  private final byte[] txInformationBlock;
  private final int txBlockSize;
  int nextTxByte = NEXT_BYTE_NOT_READ;
  private final Set<NormalResponseModeSupport.InformationFragment> unconfirmedFrames = new HashSet<NormalResponseModeSupport.InformationFragment>();


  /**
   * Creates the frame builder.
   *
   * @param block the channel.
   * @param txBlockSize the input stream.
   */
  public HdlcFraqmentBuilder(HdlcInformationBlockOut block, int txBlockSize)
  {
    this.block = block;
    this.in= block.getInformationInputStream();
    this.txBlockSize = txBlockSize;
    txInformationBlock = new byte[txBlockSize];
  }

  /**
   * Builds the next I-Frame.
   * 
   * @return The next I-Frame or null if the end of file was reached.
   * @throws IOException
   */
  public NormalResponseModeSupport.InformationFragment buildNextFraqment() throws IOException
  {
    if (nextTxByte== NEXT_BYTE_NOT_READ)
    {
      nextTxByte = in.read();
    }

    if (eof())
    {
      return null;
    }


    txInformationBlock[0] = (byte)nextTxByte;
    int bytesRead = in.read(txInformationBlock, 1, txBlockSize - 1);
    if (bytesRead < 0)
    {
      bytesRead = 0;
    }
    nextTxByte = in.read();

    return new NormalResponseModeSupport.InformationFragment(CodingUtils.copyOf(txInformationBlock, bytesRead + 1),nextTxByte >= 0, block);
  }
  
  
   /**
   * Confirms the specified frame.
   * <P>
     * A frame should only be confirmed if the frame was acknowledged by the secondary station.
   * <P>
   * If all frames are confirmed and the end of file was reached the state of the information block will
   * be set to SENT.
   *
   * @param fragment
   */
  public void confirmFragment(NormalResponseModeSupport.InformationFragment fragment)
  {
    unconfirmedFrames.remove(fragment);
    if (eof() && unconfirmedFrames.isEmpty())
    {
      if (block.getState() != HdlcInformationBlockOut.State.ERROR)
      {
        block.setState(HdlcInformationBlockOut.State.SENT);
      }
    }
  }

  /**
   * Sets the specified frame and the information block to an error state.
   *
   */
  public void errorFrame(NormalResponseModeSupport.InformationFragment fragment, IOException ex)
  {
    unconfirmedFrames.remove(fragment);
    block.setState(HdlcInformationBlockOut.State.ERROR, ex);
  }
  

  /**
   * Returns true if the end of the input stream was reached and no more I frames can be produced.
   *
   * @return {@code true} if the end of the input stream was reached.
   */
  public boolean eof()
  {
    return nextTxByte < 0;
  }

  public HdlcInformationBlockOut getBlock()
  {
    return block;
  }
  
  

}
