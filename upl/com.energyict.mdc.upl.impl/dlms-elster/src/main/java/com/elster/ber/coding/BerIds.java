/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerIds.java $
 * Version:
 * $Id: BerIds.java 4000 2012-02-09 17:08:16Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 15:50:07
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;

/**
 * Standard ids (Tags) for the "universal" types.
 *
 * @author osse
 */
public final class BerIds
{
  private BerIds()
  {
    //no instances allowed.
  }

  public static final BerId ID_BOOLEAN = new BerId(BerId.Tag.UNIVERSAL, false, 1);
  public static final BerId ID_INT = new BerId(BerId.Tag.UNIVERSAL, false, 2);
  public static final BerId ID_BITSTRING = new BerId(BerId.Tag.UNIVERSAL, false, 3);
  public static final BerId ID_OCTETSTRING = new BerId(BerId.Tag.UNIVERSAL, false, 4);
  public static final BerId ID_OID = new BerId(BerId.Tag.UNIVERSAL, false, 6);
  public static final BerId ID_SEQUENCE = new BerId(BerId.Tag.UNIVERSAL, true, 16);
  public static final BerId ID_GRAPHICSTRING = new BerId(BerId.Tag.UNIVERSAL, false, 25);
  public static final BerId ID_GRAPHIC_STRING = new BerId(BerId.Tag.UNIVERSAL, false, 25);
  public static final BerId ID_VISIBLE_STRING = new BerId(BerId.Tag.UNIVERSAL, false, 26);
}
