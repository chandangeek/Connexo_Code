/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderDlmsConformance.java $
 * Version:     
 * $Id: CoderDlmsConformance.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  06.08.2010 16:05:12
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.coding.BerDecoderBitString;
import com.elster.ber.coding.BerEncoder;
import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueBitString;
import com.elster.coding.AbstractCoder;
import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.BitStringBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * En-/decoder for the "DLMS conformance" set.
 *
 * @author osse
 */
public class CoderDlmsConformance extends AbstractCoder<EnumSet<DlmsConformance>>
{
  public static final BerId APP_31_ID = new BerId(BerId.Tag.APPLICATION, false, 31);
  private final BerDecoderBitString decBitString = new BerDecoderBitString();
  private final BerEncoder berEncoder = new BerEncoder();

  @Override
  public void encodeObject(final EnumSet<DlmsConformance> conformanceSet, final OutputStream out) throws
          IOException
  {
    DlmsConformance[] conformanceValues = DlmsConformance.values();
    BitStringBuilder conformanceBitStringBuilder = new BitStringBuilder(conformanceValues.length);

    for (DlmsConformance c : conformanceSet)
    {
      conformanceBitStringBuilder.setBit(c.ordinal(), true);
    }
    berEncoder.encode(out, new BerValueBitString(APP_31_ID, conformanceBitStringBuilder.toBitString()));

  }

  @Override
  public EnumSet<DlmsConformance> decodeObject(final InputStream in) throws IOException
  {
    BerValueBitString conformanceBerBitString = decBitString.decode(in);
    if (!APP_31_ID.equals(conformanceBerBitString.getIdentifier()))
    {
      throw new IOException("Unexpected BER Type for conformance bit string. expected: " + APP_31_ID
                            + " actual:" + conformanceBerBitString.getIdentifier());
    }

    BitString conformanceBitString = conformanceBerBitString.getValue();
    DlmsConformance[] conformanceValues = DlmsConformance.values();

    if (conformanceBitString.getBitCount() != conformanceValues.length)
    {
      throw new IOException("Unexpected number of bits in conformance bit string. expected: "
                            + conformanceValues.length + ", actual: "
                            + conformanceBerBitString.getIdentifier());
    }

    EnumSet<DlmsConformance> conformanceSet = EnumSet.noneOf(DlmsConformance.class);

    for (int i = 0; i < conformanceBitString.getBitCount(); i++)
    {
      if (conformanceBitString.isBitSet(i))
      {
        conformanceSet.add(conformanceValues[i]);
      }
    }
    return conformanceSet;
  }

}
