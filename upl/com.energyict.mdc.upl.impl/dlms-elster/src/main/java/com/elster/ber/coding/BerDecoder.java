/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoder.java $
 * Version:     
 * $Id: BerDecoder.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 11:45:22
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValue;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for decoding BER data provided by an input stream.
 *
 * @author osse
 */
public abstract class BerDecoder
{
  /**
   * Decodes the data from the input stream to an value based on {@link BerValue}.
   * <P>
   * This can result in an structure based on {@link com.elster.ber.types.BerCollection}.
   * <P>
   * This method simply calls {@link #decode(com.elster.ber.types.BerId, com.elster.ber.coding.BerInputStream)}
   * with {@code null} as {@code BerId}. The needed {@code BerInputstream}
   * will be created if necessary (with the given stream as sub layer)
   *
   * @param in The input stream
   * @return An {@code BerValue}
   * @throws IOException
   */
  public abstract BerValue decode(final InputStream in) throws IOException;

  /**
   * Decodes the data from the {@code BerInputStream} stream to an value based on {@link BerValue}.<P>
   * <P>
   * This can result in an structure based on  {@link com.elster.ber.types.BerCollection}.
   * <P>
   * If the {@code id} is {@code null} the stream must start with an tag,
   * otherwise the stream must start with the length information of the TLV structure.
   *
   * @param id An already read id or null.
   * @param in The input stream.
   * @return the decoded BerValue.
   * @throws IOException
   */
  public abstract BerValue decode(BerId id, BerInputStream in) throws IOException;

}
